package de.shopme.data.sync

import android.util.Log
import de.shopme.core.AppScope
import de.shopme.data.datasource.firestore.FirestoreDataSource
import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.model.ShoppingItemEntity
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

class SyncCoordinator(
    private val changeQueueDao: ChangeQueueDao,
    private val itemDao: ItemDao,
    private val listDao: ListDao,
    private val firestore: FirestoreDataSource,
    private val appScope: AppScope
) {

    private val isRunning = AtomicBoolean(false)

    fun start(listIdProvider: () -> String?) {

        if (isRunning.getAndSet(true)) return

        appScope.scope.launch {

            while (true) {

                val hasWork = processQueueWithResult(listIdProvider)

                if (!hasWork) {
                    delay(2000) // idle
                } else {
                    delay(300) // fast follow-up
                }
            }
        }
    }


    private suspend fun processQueue(listIdProvider: () -> String?) {

        val now = System.currentTimeMillis()

        val changes = changeQueueDao.getPending(limit = 20)

        for (change in changes) {

            if (change.entityType == "item") {

                val remoteVersion = firestore.getItemVersion(change.listId, change.entityId)

                if (remoteVersion != null && change.baseVersion != remoteVersion) {
                    handleConflict(change)
                    continue
                }
            }

            // ----------------------------
            // Backoff prüfen
            // ----------------------------
            val lastAttempt = change.lastAttemptAt ?: 0
            val backoff = calculateBackoff(change.retryCount)

            if (now - lastAttempt < backoff) {
                continue
            }

            

            try {

                changeQueueDao.updateState(change.id, "SYNCING")

                when (change.entityType) {

                    "list" -> {
                        when (change.operation) {

                            "CREATE" -> {
                                val list = listDao.getListOnce(change.entityId) ?: continue
                                firestore.createList(list)
                            }

                            "DELETE" -> {
                                firestore.deleteList(change.listId)
                            }
                        }
                    }

                    "item" -> {
                        when (change.operation) {

                            "CREATE" -> {
                                val item = itemDao.getById(change.entityId) ?: continue
                                firestore.addItem(change.listId, item)
                            }

                            "UPDATE" -> {
                                val item = itemDao.getById(change.entityId) ?: continue
                                firestore.updateItem(change.listId, item)
                            }

                            "DELETE" -> {
                                val item = itemDao.getById(change.entityId)
                                val itemListId = item?.listId

                                if (itemListId != null) {
                                    firestore.deleteItem(change.listId, change.entityId)
                                }
                            }
                        }
                    }
                }

                changeQueueDao.updateState(change.id, "DONE")

            } catch (e: Exception) {

                val newRetry = change.retryCount + 1

                changeQueueDao.updateRetry(
                    id = change.id,
                    state = if (newRetry >= 5) "FAILED" else "PENDING",
                    retryCount = newRetry,
                    timestamp = now
                )
            }
        }

        // Cleanup
        changeQueueDao.deleteCompleted()
    }

    private suspend fun handleConflict(
        change: ChangeQueueEntity
    ) {
        try {
            

            val localItem = itemDao.getById(change.entityId) ?: return

            val remoteVersion = firestore.getItemVersion(localItem.listId, localItem.id)
                ?: return

            val resolvedItem = resolveConflict(localItem, remoteVersion)

            

            itemDao.upsert(resolvedItem)

            

            changeQueueDao.updateState(
                change.id,
                "PENDING"
            )

        } catch (e: Exception) {
            

            changeQueueDao.updateState(
                change.id,
                "FAILED"
            )
        }
    }

    private fun resolveConflict(
        local: ShoppingItemEntity,
        remoteVersion: Int
    ): ShoppingItemEntity {

        return local.copy(
            version = remoteVersion + 1,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun calculateBackoff(retryCount: Int): Long {
        val baseDelay = 1000L // 1s
        val maxDelay = 60_000L // 60s

        val delay = baseDelay * (1 shl retryCount)
        return delay.coerceAtMost(maxDelay)
    }

    private suspend fun processQueueWithResult(
        listIdProvider: () -> String?
    ): Boolean {

        val changes = changeQueueDao.getPending(limit = 20)

        if (changes.isEmpty()) return false

        processQueue(listIdProvider)

        return true
    }
}