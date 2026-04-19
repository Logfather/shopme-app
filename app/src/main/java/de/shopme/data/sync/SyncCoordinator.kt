package de.shopme.data.sync

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import de.shopme.core.AppScope
import de.shopme.data.datasource.firestore.FirestoreDataSource
import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

class SyncCoordinator(
    private val changeQueueDao: ChangeQueueDao,
    private val itemDao: ItemDao,
    private val listDao: ListDao,
    private val firestore: FirestoreGateway,
    private val appScope: AppScope,
    private val firebaseAuth: FirebaseAuth // 🔥 NEU
) {

    private val isRunning = AtomicBoolean(false)

    private val activeListSyncs = mutableMapOf<String, Job>()

    private val isProcessing = AtomicBoolean(false)

    private val startMutex = Any()

    private val isShuttingDown = AtomicBoolean(false)

    fun start() {

        Log.d(
            "SYNC_GUARD",
            "start() called from ${Throwable().stackTrace.first()}"
        )

        if (!isRunning.compareAndSet(false, true)) {
            Log.d("SYNC_LIST", "Already running → skip")
            return
        }

        isShuttingDown.set(false)

        Log.d("SYNC_LIST", "START CALLED")

        appScope.scope.launch {

            Log.d("SYNC_LIST", "LOOP STARTED")

            try {
                while (isActive && isRunning.get() && !isShuttingDown.get()) {

                    val hasWork = processQueueWithResult()

                    delay(if (hasWork) 300 else 2000)
                }

            } catch (e: Exception) {
                Log.e("SYNC_LIST", "LOOP CRASH", e)
            } finally {
                Log.d("SYNC_LIST", "LOOP STOPPED")
                isRunning.set(false)
            }
        }
    }

    fun stop() {
        if (!isRunning.compareAndSet(true, false)) {
            Log.d("SYNC_LIST", "Already stopped → skip")
            return
        }

        Log.d("SYNC_LIST", "STOP CALLED")

        // 🔴 Shutdown Flag setzen
        isShuttingDown.set(true)

        // 🔴 Alle List Syncs hart abbrechen
        activeListSyncs.values.forEach { job ->
            job.cancel()
        }
        activeListSyncs.clear()
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

                    remoteItems.forEach { remote ->

                        // 🔥 GUARD: invalid remote timestamps ignorieren
                        if (remote.updatedAt <= 0L) {
                            Log.d("SYNC_SKIP", "IGNORE remote id=${remote.id} updatedAt=${remote.updatedAt}")
                            return@forEach
                        }

                        val local = itemDao.getById(remote.id)

                        when {

                            local == null -> {
                                Log.d("SYNC_APPLY", "LOCAL null → insert remote id=${remote.id}")
                                itemDao.upsert(remote)
                            }

                            remote.updatedAt > local.updatedAt -> {
                                Log.d("SYNC_APPLY", "REMOTE newer id=${remote.id}")
                                itemDao.upsert(remote)
                            }

                            remote.updatedAt < local.updatedAt -> {
                                Log.d("SYNC_RESOLVE", "LOCAL newer → keep local id=${remote.id}")
                            }

                            else -> {
                                Log.d("SYNC_RESOLVE", "EQUAL → no-op id=${remote.id}")
                            }
                        }
                    }
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



    private suspend fun processQueue(
        changes: List<ChangeQueueEntity>
    ) {

        for (change in changes) {

            // 🔁 Retry Backoff
            if (!RetryPolicy.shouldRetry(change.retryCount, change.lastAttemptAt)) {
                continue
            }

            try {
                // ------------------------------------------------------------
                // STATE GUARD
                // ------------------------------------------------------------

                val beforeState = changeQueueDao.getState(change.id)
                if (beforeState != "PENDING") continue

                changeQueueDao.markSyncing(change.id, System.currentTimeMillis())

                val afterState = changeQueueDao.getState(change.id)
                if (afterState != "SYNCING") continue

                // ------------------------------------------------------------
                // ENTITY SWITCH
                // ------------------------------------------------------------

                when (change.entityType) {

                    // ============================================================
                    // ITEMS
                    // ============================================================

                    "item" -> {

                        val item = itemDao.getById(change.entityId)

                        if (item == null) {
                            Log.w("SYNC_ITEM", "Missing local → DONE id=${change.entityId}")
                            changeQueueDao.updateState(change.id, "DONE")
                            continue
                        }

                        // 🔍 Conflict Check (nur CREATE / UPDATE)
                        val remoteVersion = firestore.getItemVersion(item.listId, item.id)

                        when (change.operation) {

                            "CREATE" -> {
                                // 🔥 CREATE darf NIE durch Version blockiert werden
                                // immer senden
                            }

                            "UPDATE" -> {
                                if (remoteVersion != null && remoteVersion != change.baseVersion) {
                                    Log.d(
                                        "SYNC_UPDATE",
                                        "VERSION local=${change.baseVersion} remote=$remoteVersion id=${change.entityId}"
                                    )
                                    handleConflict(change, remoteVersion)
                                    continue
                                }
                            }

                            "DELETE" -> {
                                // kein conflict check
                            }
                        }

                        // --------------------------------------------------------
                        // EXECUTION
                        // --------------------------------------------------------

                        val success = when (change.operation) {

                            "CREATE" -> {
                                Log.d("SYNC_CREATE", "START id=${item.id}")
                                firestore.addItem(item.listId, item)
                            }

                            "UPDATE" -> {
                                Log.d("SYNC_UPDATE", "START id=${item.id}")
                                firestore.updateItem(item.listId, item)
                            }

                            "DELETE" -> {
                                Log.d("SYNC_DELETE", "START id=${item.id}")
                                firestore.deleteItem(item.listId, item.id)
                            }

                            else -> false
                        }

                        // --------------------------------------------------------
                        // COMMIT
                        // --------------------------------------------------------

                        if (success) {
                            Log.d("SYNC_DONE", "SUCCESS id=${change.entityId}")
                            changeQueueDao.markDoneByEntityId(change.entityId)
                        } else {
                            throw Exception("Sync failed: ${change.operation}")
                        }
                    }

                    // ============================================================
                    // LISTS
                    // ============================================================

                    "list" -> {

                        val success = when (change.operation) {

                            "CREATE" -> {

                                val uid = firebaseAuth.currentUser?.uid
                                    ?: throw Exception("User not authenticated")

                                val payload = change.payload
                                    ?: throw Exception("Missing payload")

                                val list = Gson().fromJson(payload, ShoppingListEntity::class.java)

                                val created = firestore.createList(list, uid)
                                if (!created) false

                                firestore.addMembership(uid, list.id)
                                true
                            }

                            "DELETE" -> {
                                firestore.softDeleteList(change.listId)
                                true
                            }

                            else -> false
                        }

                        if (success) {
                            changeQueueDao.updateState(change.id, "DONE")
                        } else {
                            throw Exception("List sync failed")
                        }
                    }

                    // ============================================================
                    // MEMBERSHIP
                    // ============================================================

                    "membership" -> {

                        val userId = change.payload ?: continue

                        val success = runCatching {
                            firestore.addUserToList(
                                listId = change.listId,
                                userId = userId
                            )
                        }.isSuccess

                        if (success) {
                            changeQueueDao.updateState(change.id, "DONE")
                        } else {
                            throw Exception("Membership failed")
                        }
                    }

                    // ============================================================
                    // INVITE
                    // ============================================================

                    "invite" -> {

                        val inviteId = change.payload ?: continue

                        val success = runCatching {
                            firestore.markInviteConsumed(inviteId)
                        }.isSuccess

                        if (success) {
                            changeQueueDao.updateState(change.id, "DONE")
                        } else {
                            throw Exception("Invite failed")
                        }
                    }
                }

            } catch (e: Exception) {

                Log.e("SYNC_ERROR", "FAILED ${change.entityType} ${change.entityId}", e)

                val newRetry = change.retryCount + 1

                if (newRetry >= 5) {
                    changeQueueDao.updateState(change.id, "FAILED")
                } else {
                    changeQueueDao.updateRetry(
                        id = change.id,
                        state = "PENDING",
                        retryCount = newRetry,
                        timestamp = System.currentTimeMillis()
                    )
                }
            }
        }

        // ------------------------------------------------------------
        // CLEANUP (delayed)
        // ------------------------------------------------------------

        appScope.scope.launch {
            delay(1500)
            changeQueueDao.deleteCompleted()
        }
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
                "DONE"
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

        if (!isProcessing.compareAndSet(false, true)) {
            Log.d("QUEUE_SKIP", "Already processing → skip")
            return false
        }

        try {

            val changes = changeQueueDao.getPending(limit = 20)

            Log.d("QUEUE_DEBUG", "FETCH dao=${changeQueueDao.hashCode()} size=${changes.size}")

            changes.forEach {
                Log.d("SYNC_DEBUG", "Queue item: ${it.operation} ${it.entityType} ${it.entityId} state=${it.state}")
            }

            if (changes.isEmpty()) {
                Log.d("QUEUE_IDLE", "No pending work")
                return false
            }

            processQueue(changes) // ✅ NUR HIER

            return true

        } finally {
            isProcessing.set(false)
        }
    }

//    suspend fun retryItem(itemId: String) {
//        changeQueueDao.markPendingByEntityId(itemId)
//        processQueue()
//    }


}