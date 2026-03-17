package de.shopme.data.sync

import android.util.Log
import de.shopme.data.datasource.firestore.FirestoreDataSource
import de.shopme.data.datasource.room.ItemDao
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

class SyncCoordinator(
    private val changeQueueDao: ChangeQueueDao,
    private val itemDao: ItemDao,
    private val firestore: FirestoreDataSource
) {

    private val isRunning = AtomicBoolean(false)

    fun start(listIdProvider: () -> String?) {

        if (isRunning.getAndSet(true)) return

        CoroutineScope(Dispatchers.IO).launch {

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

        val listId = listIdProvider() ?: return

        val now = System.currentTimeMillis()

        val changes = changeQueueDao.getPending(limit = 20)

        for (change in changes) {

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

                when (change.operation) {

                    "CREATE" -> {
                        val item = itemDao.getById(change.entityId) ?: continue
                        firestore.addItem(listId, item)
                    }

                    "UPDATE" -> {
                        val item = itemDao.getById(change.entityId) ?: continue
                        firestore.updateItem(listId, item)
                    }

                    "DELETE" -> {
                        firestore.deleteItem(listId, change.entityId)
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

    private fun calculateBackoff(retryCount: Int): Long {
        val baseDelay = 1000L // 1s
        val maxDelay = 60_000L // 60s

        val delay = baseDelay * (1 shl retryCount)
        return delay.coerceAtMost(maxDelay)
    }

    private suspend fun processQueueWithResult(
        listIdProvider: () -> String?
    ): Boolean {

        val listId = listIdProvider() ?: return false

        val changes = changeQueueDao.getPending(limit = 20)

        if (changes.isEmpty()) return false

        processQueue(listIdProvider)

        return true
    }
}