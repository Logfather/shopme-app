package de.shopme.data.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.logging.RecoveryLog
import de.shopme.data.sync.logging.SyncLog
import de.shopme.data.sync.queue.ChangeQueueDao
import de.shopme.data.sync.queue.ChangeQueueEntity
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean

class SyncCoordinator(
    private val changeQueueDao: ChangeQueueDao,
    private val itemDao: ItemDao,
    private val listDao: ListDao,
    val firestore: FirestoreGateway,
    private val appScope: CoroutineScope,
    private val firebaseAuth: FirebaseAuth?,
    private val conflictResolver: ConflictResolver,
    private val roomRepository: RoomShoppingRepository,
    private val syncDebounceMs: Long = 250
) {

    private val syncMutex = Mutex()

    private var syncJob: Job? = null

    private val isRunning = AtomicBoolean(false)

    private val activeListSyncs = mutableMapOf<String, Job>()

    private val isProcessing = AtomicBoolean(false)

    private val processMutex = Mutex()

    private val isShuttingDown = AtomicBoolean(false)



    fun triggerSync(
        force: Boolean = false
    ) {

        if (syncJob?.isActive == true) {

            SyncLog.orchestrator(
                "[Guard] Sync already active -> skip trigger"
            )

            return
        }

        syncJob = appScope.launch {

            try {

                SyncLog.orchestrator(
                    "[Lifecycle] Sync job start force=$force"
                )

                if (
                    !force &&
                    syncDebounceMs > 0
                ) {
                    delay(syncDebounceMs)
                }

                SyncLog.orchestrator(
                    "[Lifecycle] Sync after debounce"
                )

                syncMutex.withLock {

                    SyncLog.orchestrator(
                        "[Mutex] Enter sync mutex"
                    )

                    processQueueSequential(force)

                    SyncLog.orchestrator(
                        "[Mutex] Exit sync mutex"
                    )
                }

                SyncLog.orchestrator(
                    "[Lifecycle] Sync job end"
                )

            } catch (e: Exception) {

                RecoveryLog.processError(
                    "Sync job crash",
                    e
                )
            }
        }
    }

    private suspend fun processQueueSequential(
        force: Boolean
    ) {
        SyncLog.queue(
            "[Lifecycle] processQueueSequential ENTER"
        )

        while (true) {

            val next =
                changeQueueDao.getOldestPendingChange()

            if (
                !force &&
                next?.nextRetryAt != null &&
                System.currentTimeMillis() < next.nextRetryAt
            ) {

                RecoveryLog.policy(
                    "[Retry] Backoff active -> break"
                )

                break
            }

            SyncLog.queue(
                "[Fetch] oldestPendingChange=$next"
            )

            if (next == null) {

                RecoveryLog.policy(
                    "[Retry] Backoff active -> break"
                )

                break
            }

            SyncLog.queue(
                "[Process] op=${next.operation} entity=${next.entityId}"
            )

            SyncLog.queue(
                "[Before] id=${next.id} state=${next.state} op=${next.operation}"
            )

            processSingleChange(next)

            val after =
                changeQueueDao.getChangeById(next.id)

            SyncLog.queue(
                "[After] id=${after?.id} state=${after?.state}"
            )
        }

        SyncLog.queue(
            "[Lifecycle] processQueueSequential EXIT"
        )
    }

    private suspend fun processSingleChange(
        change: ChangeQueueEntity
    ) {

        SyncLog.queue(
            "[Single] ENTER op=${change.operation}"
        )

        try {

            processMutex.withLock {

                processQueue(listOf(change))
            }

        } catch (e: Exception) {

            val retryCount =
                change.retryCount + 1

            val now =
                System.currentTimeMillis()

            val retryDelayMs =
                minOf(
                    retryCount * 5000L,
                    60000L
                )

            val nextRetryAt =
                now + retryDelayMs

            RecoveryLog.policyError(
                "Retry scheduled retry=$retryCount delay=$retryDelayMs",
                e
            )

            changeQueueDao.updateRetry(
                id = change.id,
                state = "PENDING",
                retryCount = retryCount,
                timestamp = now,
                nextRetryAt = nextRetryAt
            )
        }
    }

    fun start() {

        SyncLog.orchestrator(
            "[Guard] start() called from ${Throwable().stackTrace.first()}"
        )

        if (!isRunning.compareAndSet(false, true)) {
            SyncLog.orchestrator(
                "[Guard] Already running -> skip"
            )
            return
        }

        isShuttingDown.set(false)

        SyncLog.queue(
            "[Lifecycle] START called"
        )

        appScope.launch {

            SyncLog.queue(
                "[Loop] STARTED"
            )

            try {
                while (isActive && isRunning.get() && !isShuttingDown.get()) {

                    val hasWork = processQueueWithResult()

                    val delayMs = if (hasWork) {
                        200L
                    } else {
                        1500L + (0..500).random()
                    }

                    delay(delayMs)
                }

            } catch (e: Exception) {
                RecoveryLog.processError(
                    "Queue loop crash",
                    e
                )
            } finally {
                SyncLog.queue(
                    "[Loop] STOPPED"
                )
                isRunning.set(false)
            }
        }
    }

    fun stop() {
        if (!isRunning.compareAndSet(true, false)) {
            SyncLog.orchestrator(
                "[Guard] Already stopped -> skip"
            )
            return
        }

        SyncLog.queue(
            "[Lifecycle] STOP called"
        )

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
            SyncLog.queue(
                "[Guard] Sync already running list=$listId"
            )
            return
        }

        SyncLog.queue(
            "[Lifecycle] Start sync list=$listId"
        )

        val job = appScope.launch {

            // 🔁 LIST FLOW
            launch {
                firestore.observeListById(listId).collect { list ->
                    if (list != null) {
                        listDao.upsert(list)
                    }
                }
            }

            // 🔁 ITEM FLOW
            launch {
                firestore.observeItems(listId).collect { remoteItems ->

                    SyncLog.queue(
                        "[Flow] ITEM emit list=$listId size=${remoteItems.size}"
                    )

                    remoteItems.forEach { remote ->

                        if (remote.updatedAt <= 0L) {
                            SyncLog.orchestrator(
                                "[Guard] IGNORE remote id=${remote.id} updatedAt=${remote.updatedAt}"
                            )
                            return@forEach
                        }

                        val local = itemDao.getById(remote.id)?.copy()

                        // 🔒 DUPLICATE GUARD (NEU)
                        if (
                            local != null &&
                            remote.updatedAt == local.updatedAt &&
                            remote.deletedAt == local.deletedAt &&
                            remote.name == local.name &&
                            remote.isChecked == local.isChecked
                        ) {
                            SyncLog.orchestrator(
                                "[Guard] IDENTICAL skip id=${remote.id}"
                            )
                            return@forEach
                        }

                        // 🔥 DELETE hat absolute Priorität
                        if (remote.deletedAt != null) {

                            SyncLog.apply(
                                "[Delete] REMOTE id=${remote.id}"
                            )

                            // 🔒 zusätzliche Absicherung gegen unnötige Writes
                            if (local == null || local.deletedAt != remote.deletedAt) {
                                itemDao.upsert(remote)
                            } else {
                                SyncLog.orchestrator(
                                    "[Guard] DELETE already applied id=${remote.id}"
                                )
                            }

                            return@forEach
                        }

                        when {

                            local == null -> {
                                SyncLog.apply(
                                    "[Insert] LOCAL null -> insert remote id=${remote.id}"
                                )
                                itemDao.upsert(remote)
                            }

                            remote.updatedAt > local.updatedAt -> {
                                SyncLog.apply(
                                    "[Apply] REMOTE newer id=${remote.id}"
                                )
                                itemDao.upsert(remote)
                            }

                            remote.updatedAt < local.updatedAt -> {
                                SyncLog.apply(
                                    "[Resolve] LOCAL newer -> keep local id=${remote.id}"
                                )
                            }

                            else -> {
                                if (remote != local) {
                                    SyncLog.conflict(
                                        "[Repair] Same version but different content -> apply remote id=${remote.id}"
                                    )
                                    itemDao.upsert(remote)
                                } else {
                                    SyncLog.apply(
                                        "[Resolve] EQUAL no-op id=${remote.id}"
                                    )
                                }
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
            SyncLog.queue(
                "[Lifecycle] Stop sync list=$listId"
            )
            job.cancel()
            activeListSyncs.remove(listId)
        } else {
            SyncLog.queue(
                "[Guard] No active sync list=$listId"
            )
        }
    }

    suspend fun deleteLocalList(listId: String) {

        SyncLog.queue(
            "[Delete] Local list=$listId"
        )

        itemDao.deleteByListId(listId)
        listDao.deleteById(listId)
    }

    fun deleteLocalListAsync(listId: String) {

        appScope.launch {

            SyncLog.queue(
                "[DeleteAsync] Local list=$listId"
            )

            deleteLocalList(listId)
        }
    }



    private suspend fun processQueue(
        changes: List<ChangeQueueEntity>
    ) {

        if (changes.isEmpty()) {
            return
        }

        for (change in changes) {

            // 🔁 Retry Backoff
            val now = System.currentTimeMillis()

            if (!RetryPolicy.shouldRetry(change.retryCount, change.lastAttemptAt)) {
                RecoveryLog.policy(
                    "[RetrySkip] id=${change.id} retry=${change.retryCount}"
                )
                continue
            }

            try {
                // ------------------------------------------------------------
                // STATE GUARD
                // ------------------------------------------------------------

                val beforeState = changeQueueDao.getState(change.id)
                if (beforeState != "PENDING") continue

                changeQueueDao.markSyncing(change.id, now)

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

                        val itemNullable = itemDao.getById(change.entityId)

                        // 🔥 DELETE darf auch ohne Local Entity ausgeführt werden
                        if (itemNullable == null && change.operation != "DELETE") {

                            RecoveryLog.policy(
                                "[MissingLocal] RETRY id=${change.entityId} op=${change.operation}"
                            )

                            val newRetry = change.retryCount + 1
                            val now = System.currentTimeMillis()

                            if (newRetry >= 5) {

                                RecoveryLog.processError(
                                    "FINAL FAIL id=${change.entityId}"
                                )

                                changeQueueDao.updateState(change.id, "FAILED")

                            } else {

                                val delay = RetryPolicy.computeDelay(newRetry)

                                RecoveryLog.policy(
                                    "[Retry] id=${change.entityId} delay=${delay} retry=$newRetry"
                                )

                                val retryDelayMs =
                                    minOf(
                                        newRetry * 5000L,
                                        60000L
                                    )

                                val nextRetryAt =
                                    now + retryDelayMs

                                changeQueueDao.updateRetry(
                                    id = change.id,
                                    state = "PENDING",
                                    retryCount = newRetry,
                                    timestamp = now,
                                    nextRetryAt = nextRetryAt
                                )
                            }

                            continue
                        }

                        // 🔥 ab hier garantiert non-null für CREATE / UPDATE
                        val item = itemNullable

                        // 🔍 Conflict Check (nur CREATE / UPDATE)
                        val remoteVersion = firestore.getItemVersion(item!!.listId, item!!.id)

                        // 🔍 Conflict Check (nur CREATE / UPDATE)

                        when (change.operation) {

                            "UPDATE" -> {

                                val itemSafe = item
                                    ?: throw Exception("UPDATE without local item id=${change.entityId}")

                                val remote = firestore.getItem(itemSafe.listId, itemSafe.id)

                                if (remote != null && remote.updatedAt > itemSafe.updatedAt) {

                                    SyncLog.orchestrator(
                                        "[Guard] Skip outdated update id=${itemSafe.id}"
                                    )

                                    changeQueueDao.updateState(change.id, "DONE")
                                    continue
                                }
                            }

                            else -> {
                                // CREATE & DELETE → nichts hier machen
                            }
                        }
                        SyncLog.apply(
                            "[Execute] op=${change.operation} id=${item.id} baseVersion=${change.baseVersion}"
                        )

                        // --------------------------------------------------------
                        // EXECUTION
                        // --------------------------------------------------------

                        val success = when (change.operation) {

                            "CREATE" -> {

                                val targetId = change.entityId
                                val targetListId = change.listId

                                val localItem = itemDao.getById(targetId)
                                    ?: throw Exception("CREATE without local item id=$targetId")

                                // 🔒 PREVENT DOUBLE CREATE
                                val remote = firestore.getItem(targetListId, targetId)
                                if (remote != null) {
                                    SyncLog.orchestrator(
                                        "[Guard] CREATE already exists remotely id=$targetId"
                                    )
                                    changeQueueDao.updateState(change.id, "DONE")
                                    continue
                                }

                                val success = firestore.addItem(targetListId, localItem)

                                if (!success) {
                                    throw Exception("CREATE failed id=$targetId")
                                }

                                changeQueueDao.updateState(change.id, "DONE")
                                continue
                            }

                            "UPDATE" -> {

                                val targetId = change.entityId
                                val targetListId = change.listId

                                val localItem = itemDao.getById(targetId)
                                    ?: throw Exception("UPDATE without local item id=$targetId")

                                val remote = firestore.getItem(targetListId, targetId)

                                // ============================================================
                                // 🔥 HARD VERSION CHECK (PHASE 3 CORE)
                                // ============================================================

                                val remoteVersion = remote?.updatedAt ?: 0L
                                val baseVersion = change.baseVersion

                                if (remote != null && remoteVersion != baseVersion) {

                                    SyncLog.conflict(
                                        "[VersionMismatch] id=$targetId base=$baseVersion remote=$remoteVersion"
                                    )

                                    // 👉 Conflict Resolver entscheidet
                                    val resolved = conflictResolver.resolveItemConflict(
                                        local = localItem,
                                        remote = remote,
                                        baseVersion = baseVersion
                                    )

                                    when (resolved.strategy) {

                                        ConflictStrategy.USE_REMOTE -> {

                                            SyncLog.conflict(
                                                "[ApplyRemote] id=$targetId"
                                            )

                                            itemDao.upsert(resolved.resolvedItem!!)
                                            changeQueueDao.updateState(change.id, "DONE")
                                            continue
                                        }

                                        ConflictStrategy.USE_LOCAL -> {

                                            SyncLog.conflict(
                                                "[ForceLocal] overwrite id=$targetId"
                                            )

                                            val success = firestore.updateItem(targetListId, localItem)

                                            if (!success) {
                                                throw Exception("UPDATE failed id=${localItem.id}")
                                            }

                                            roomRepository.onSyncWriteSuccess(localItem.id, localItem.updatedAt)

                                            changeQueueDao.updateState(change.id, "DONE")
                                            continue
                                        }

                                        ConflictStrategy.MERGE -> {

                                            SyncLog.conflict(
                                                "[Merge] id=$targetId"
                                            )

                                            val merged = resolved.resolvedItem
                                                ?: throw Exception("Merge produced null")

                                            val success = firestore.updateItem(targetListId, merged)

                                            if (!success) {
                                                throw Exception("MERGE failed id=${merged.id}")
                                            }

                                            itemDao.upsert(merged)
                                            roomRepository.onSyncWriteSuccess(merged.id, merged.updatedAt)

                                            changeQueueDao.updateState(change.id, "DONE")
                                            continue
                                        }
                                    }
                                }

                                // ============================================================
                                // 🔥 NO CONFLICT → NORMAL FLOW
                                // ============================================================

                                if (remote != null && remote.updatedAt == localItem.updatedAt) {

                                    SyncLog.orchestrator(
                                        "[Guard] Skip identical write id=$targetId"
                                    )

                                    changeQueueDao.updateState(change.id, "DONE")
                                    continue
                                }

                                SyncLog.apply(
                                    "[Write] FINAL UPDATE id=${localItem.id}"
                                )

                                val success = firestore.updateItem(targetListId, localItem)

                                if (!success) {
                                    throw Exception("UPDATE failed id=${localItem.id}")
                                }

                                roomRepository.onSyncWriteSuccess(localItem.id, localItem.updatedAt)

                                changeQueueDao.updateState(change.id, "DONE")
                                continue
                            }

                            "DELETE" -> {

                                val targetId = change.entityId
                                val targetListId = change.listId

                                SyncLog.apply(
                                    "[Write] FINAL DELETE id=$targetId"
                                )

                                val remote =
                                    firestore.getItem(
                                        targetListId,
                                        targetId
                                    )

                                if (remote == null) {

                                    changeQueueDao.updateState(
                                        change.id,
                                        "DONE"
                                    )

                                    continue
                                }

                                val tombstone =
                                    remote.copy(
                                        deletedAt = System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis()
                                    )

                                val success =
                                    firestore.updateItem(
                                        targetListId,
                                        tombstone
                                    )

                                if (!success) {

                                    throw Exception(
                                        "DELETE tombstone failed id=$targetId"
                                    )
                                }

                                changeQueueDao.updateState(
                                    change.id,
                                    "DONE"
                                )

                                continue
                            }

                            else -> false
                        }

                        // --------------------------------------------------------
                        // COMMIT
                        // --------------------------------------------------------

                        if (success) {

                            SyncLog.apply(
                                "[Done] SUCCESS id=${change.entityId} op=${change.operation}"
                            )

                            changeQueueDao.updateState(change.id, "DONE")

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

                                val uid = firebaseAuth?.currentUser?.uid
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

                RecoveryLog.processError(
                    "FAILED ${change.entityType} ${change.entityId}",
                    e
                )

                val newRetry = change.retryCount + 1

                if (newRetry >= 5) {
                    changeQueueDao.updateState(change.id, "FAILED")
                } else {
                    val retryDelayMs =
                        minOf(
                            newRetry * 5000L,
                            60000L
                        )

                    val nextRetryAt =
                        now + retryDelayMs

                    changeQueueDao.updateRetry(
                        id = change.id,
                        state = "PENDING",
                        retryCount = newRetry,
                        timestamp = now,
                        nextRetryAt = nextRetryAt
                    )
                }
            }
        }

        // ------------------------------------------------------------
        // CLEANUP (delayed)
        // ------------------------------------------------------------

        appScope.launch {
            delay(1500)
            changeQueueDao.deleteCompleted()
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

        val start = System.currentTimeMillis()

        if (!isProcessing.compareAndSet(false, true)) {
            SyncLog.queue(
                "[Guard] Already processing -> skip"
            )
            return false
        }

        try {

            val changes = changeQueueDao.getPending(limit = 10)

            SyncLog.queue(
                "[Fetch] dao=${changeQueueDao.hashCode()} size=${changes.size}"
            )

            changes.forEach {
                SyncLog.queue(
                    "[Item] ${it.operation} ${it.entityType} ${it.entityId} state=${it.state}"
                )
            }

            if (changes.isEmpty()) {
                SyncLog.queue(
                    "[Idle] No pending work"
                )
                return false
            }

            // 🔥 WICHTIG: KEIN delay VOR Verarbeitung (Race vermeiden)

            for (change in changes) {
                processSingleChange(change)
            }

            val duration = System.currentTimeMillis() - start

            SyncLog.queue(
                "[Metric] processed=${changes.size} durationMs=$duration"
            )

            return true

        } finally {
            isProcessing.set(false)
        }
    }

    suspend fun awaitIdle() {

        syncJob?.join()
    }
}