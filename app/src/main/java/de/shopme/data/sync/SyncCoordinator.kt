package de.shopme.data.sync

import android.util.Log
import de.shopme.core.AppScope
import de.shopme.data.datasource.firestore.FirestoreDataSource
import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
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

    fun start() {

        Log.d("SYNC", "START CALLED")

        if (isRunning.getAndSet(true)) return

        appScope.scope.launch {

            Log.d("SYNC", "LOOP STARTED")

            while (true) {
                try {

                    Log.d("SYNC", "Checking queue...")

                    val hasWork = processQueueWithResult()

                    if (!hasWork) {
                        delay(2000)
                    } else {
                        delay(300)
                    }

                } catch (e: Exception) {
                    Log.e("SYNC", "LOOP CRASH", e)
                    delay(2000)
                }
            }
        }
    }

    private suspend fun processQueue() {

        val changes = changeQueueDao.getPending(limit = 20)

        for (change in changes) {

            // 🔁 Retry Backoff
            if (!RetryPolicy.shouldRetry(change.retryCount, change.lastAttemptAt)) {
                continue
            }

            try {

                changeQueueDao.markSyncing(
                    change.id,
                    System.currentTimeMillis()
                )

                when (change.entityType) {

                    // ============================================================
                    // LISTS
                    // ============================================================

                    "list" -> {
                        when (change.operation) {

                            "CREATE" -> {
                                val list = listDao.getListOnce(change.entityId) ?: continue
                                firestore.createList(list)
                                changeQueueDao.updateState(change.id, "DONE")
                            }

                            "DELETE" -> {

                                Log.d("SYNC", "Processing DELETE for list=${change.listId}")

                                // 👉 falls deine Methode anders heißt, HIER anpassen
                                firestore.softDeleteList(change.listId)

                                changeQueueDao.updateState(change.id, "DONE")

                                Log.d("SYNC", "DELETE SUCCESS → DONE")
                            }
                        }
                    }

                    // ============================================================
                    // ITEMS
                    // ============================================================

                    "item" -> {

                        // 🔍 Conflict Check (nur bei CREATE / UPDATE)
                        if (change.operation != "DELETE") {

                            val remoteVersion =
                                firestore.getItemVersion(change.listId, change.entityId)

                            if (remoteVersion != null && remoteVersion.toLong() != change.baseVersion) {
                                handleConflict(change, remoteVersion.toLong())
                                continue
                            }
                        }

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
                                if (item != null) {
                                    firestore.deleteItem(change.listId, change.entityId)
                                }
                            }
                        }

                        changeQueueDao.updateState(change.id, "DONE")
                    }
                }

            } catch (e: Exception) {

                val now = System.currentTimeMillis()
                val newRetry = change.retryCount + 1

                Log.e("SYNC", "Sync failed id=${change.id} retry=$newRetry", e)

                if (newRetry >= 5) {

                    // 👉 da markFailed fehlt → fallback auf FAILED state
                    changeQueueDao.updateState(change.id, "FAILED")

                    continue
                }

                changeQueueDao.updateRetry(
                    id = change.id,
                    state = "PENDING",
                    retryCount = newRetry,
                    timestamp = now
                )
            }
        }

        changeQueueDao.deleteCompleted()
    }

    private suspend fun handleConflict(
        change: ChangeQueueEntity,
        remoteVersion: Long
    ) {
        try {

            val localItem = itemDao.getById(change.entityId) ?: return

            val resolvedItem = resolveConflict(localItem, remoteVersion)

            itemDao.upsert(resolvedItem)

            changeQueueDao.updateState(
                change.id,
                "PENDING"
            )

        } catch (e: Exception) {

            Log.e("SYNC", "Conflict handling failed", e)
            changeQueueDao.updateState(
                change.id,
                "FAILED"
            )
        }
    }

    private fun resolveConflict(
        local: ShoppingItemEntity,
        remoteUpdatedAt: Long
    ): ShoppingItemEntity {

        return if (remoteUpdatedAt > local.updatedAt) {
            local
        } else {
            local.copy(
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    private suspend fun processQueueWithResult(): Boolean {

        val changes = changeQueueDao.getPending(limit = 20)
        Log.d("SYNC", "Queue size=${changes.size}")

        if (changes.isEmpty()) return false

        processQueue()

        return true
    }
}