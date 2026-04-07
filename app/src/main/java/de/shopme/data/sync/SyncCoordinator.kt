package de.shopme.data.sync

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
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
    private val appScope: AppScope,
    private val firebaseAuth: FirebaseAuth // 🔥 NEU
) {

    private val isRunning = AtomicBoolean(false)

    private val activeListSyncs = mutableMapOf<String, Job>()

    fun start() {

        if (!isRunning.compareAndSet(false, true)) {
            Log.d("SYNC_LIST", "Already running → skip")
            return
        }

        Log.d("SYNC_LIST", "START CALLED")

        appScope.scope.launch {

            Log.d("SYNC_LIST", "LOOP STARTED")

            while (isActive) {

                try {

                    val hasWork = processQueueWithResult()

                    delay(if (hasWork) 300 else 2000)

                } catch (e: Exception) {

                    Log.e("SYNC_LIST", "LOOP CRASH", e)

                    delay(2000)
                }
            }

            Log.d("SYNC_LIST", "LOOP STOPPED")
        }
    }

    fun startSingleListSync(listId: String) {

        if (activeListSyncs.containsKey(listId)) {
            Log.d("SYNC_LIST", "Sync already running for list=$listId")
            return
        }

        Log.d("SYNC_LIST", "Start sync for list=$listId")

        val job = appScope.scope.launch {

            // 🔁 LIST FLOW
            launch {
                firestore.observeListById(listId).collect { list ->
                    Log.d("SYNC_DEBUG", "LIST FLOW EMIT: $listId -> $list")
                    if (list != null) {
                        listDao.upsert(list)
                    }
                }
            }

            // 🔁 ITEM FLOW
            launch {
                firestore.observeItems(listId).collect { remoteItems ->

                    Log.d("SYNC_DEBUG", "ITEM FLOW EMIT: $listId size=${remoteItems.size}")

                    val pendingIds = changeQueueDao
                        .getPending(limit = 1000)
                        .filter { it.entityType == "item" }
                        .map { it.entityId }
                        .toSet()

                    val safeItems = remoteItems.filterNot { it.id in pendingIds }

                    Log.d(
                        "SYNC_DEBUG",
                        "Filtered remote items: ${remoteItems.size} → ${safeItems.size}"
                    )

                    itemDao.insertAll(safeItems)
                }
            }
        }

        activeListSyncs[listId] = job
    }

    fun stopSingleListSync(listId: String) {

        val job = activeListSyncs[listId]

        if (job != null) {
            Log.d("SYNC_LIST", "Stop sync for list=$listId")
            job.cancel()
            activeListSyncs.remove(listId)
        } else {
            Log.d("SYNC_LIST", "No active sync for list=$listId")
        }
    }

    suspend fun deleteLocalList(listId: String) {

        Log.d("SYNC_LIST", "Deleting local list=$listId")

        itemDao.deleteByListId(listId)
        listDao.deleteById(listId)
    }

    fun deleteLocalListAsync(listId: String) {

        appScope.scope.launch {

            Log.d("SYNC_LIST", "Async delete local list=$listId")

            deleteLocalList(listId)
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

                val beforeState = changeQueueDao.getState(change.id)

                if (beforeState != "PENDING") {
                    continue
                }

                changeQueueDao.markSyncing(
                    change.id,
                    System.currentTimeMillis()
                )

                val afterState = changeQueueDao.getState(change.id)

                if (afterState != "SYNCING") {
                    continue
                }

                when (change.entityType) {

                    // ============================================================
                    // LISTS
                    // ============================================================

                    "list" -> {
                        when (change.operation) {

                            "CREATE" -> {

                                val list = listDao.getListOnce(change.entityId) ?: continue

                                val uid = firebaseAuth.currentUser?.uid

                                if (uid == null) {
                                    Log.e("SHARE", "User not authenticated → abort invite")
                                    return
                                }

                                firestore.createList(list, uid)

                                firestore.addMembership(
                                    userId = uid,
                                    listId = list.id
                                )

                                changeQueueDao.updateState(change.id, "DONE")
                            }

                            "DELETE" -> {
                                firestore.softDeleteList(change.listId)
                                changeQueueDao.updateState(change.id, "DONE")
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

                            if (remoteVersion != null && remoteVersion != change.baseVersion) {
                                handleConflict(change, remoteVersion)
                                continue
                            }
                        }

                        when (change.operation) {

                            "CREATE" -> {

                                val isMember = firestore.isUserMemberOfList(
                                    userId = FirebaseAuth.getInstance().uid!!,
                                    listId = change.listId
                                )

                                if (!isMember) {
                                    Log.w("SYNC_SKIP", "Skip item, not member yet")
                                    continue
                                }

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

                    // ============================================================
                    // MEMBERSHIP
                    // ============================================================

                    "membership" -> {

                        when (change.operation) {

                            "ADD" -> {

                                val uid = firebaseAuth.currentUser?.uid

                                if (uid == null) {
                                    Log.e("SYNC_MEMBERSHIP", "User not authenticated")
                                    continue
                                }

                                firestore.addUserToList(
                                    listId = change.listId,
                                    userId = uid
                                )

                                changeQueueDao.updateState(change.id, "DONE")
                            }
                        }
                    }
                }

            } catch (e: Exception) {

                Log.e("SYNC_ERROR", "Sync failed for ${change.entityType} ${change.entityId}", e)

                val now = System.currentTimeMillis()
                val newRetry = change.retryCount + 1

                if (newRetry >= 5) {
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

        Log.d("SYNC_DEBUG", "Queue fetched size=${changes.size}")

        changes.forEach {
            Log.d("SYNC_DEBUG", "Queue item: ${it.operation} ${it.entityType} ${it.entityId} state=${it.state}")
        }

        if (changes.isEmpty()) return false

        processQueue()

        return true
    }

    fun startMembershipSync(userId: String) {

        appScope.scope.launch {

            firestore.observeMemberships(userId)
                .collect { listIds ->

                    Log.d("SYNC_MEMBERSHIP", "Membership update: $listIds")

                    val currentSyncs = activeListSyncs.keys.toSet()
                    val newIds = listIds.toSet()

                    // 🔥 1. Nur NEUE starten
                    val toStart = newIds - currentSyncs

                    toStart.forEach { listId ->
                        Log.d("SYNC_MEMBERSHIP", "Start sync for NEW list=$listId")
                        startSingleListSync(listId)
                    }

                    // 🔥 2. OPTIONAL: Stop nur wenn wirklich entfernt
                    val toStop = currentSyncs - newIds

                    toStop.forEach { listId ->
                        Log.d("SYNC_MEMBERSHIP", "Stop sync for REMOVED list=$listId")
                        stopSingleListSync(listId)
                    }
                }
        }
    }
}