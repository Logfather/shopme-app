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
import de.shopme.data.sync.telemetry.SyncRuntimeDiagnosticsLogger
import de.shopme.data.sync.telemetry.SyncRuntimeDiagnosticsProvider
import de.shopme.data.sync.telemetry.SyncTelemetryCollector
import de.shopme.data.sync.telemetry.SyncTelemetryEvent
import de.shopme.data.sync.telemtry.ReplayOutcome
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SyncCoordinator(
    private val changeQueueDao: ChangeQueueDao,
    private val itemDao: ItemDao,
    private val listDao: ListDao,
    val firestore: FirestoreGateway,
    private val appScope: CoroutineScope,
    private val firebaseAuth: FirebaseAuth?,
    private val conflictResolver: ConflictResolver,
    private val roomRepository: RoomShoppingRepository,
    private val remoteApplyCoordinator: RemoteApplyCoordinator,
    private val syncDebounceMs: Long = 250,
    private val telemetry: SyncTelemetryCollector,
    private val diagnosticsProvider: SyncRuntimeDiagnosticsProvider,
    private val diagnosticsLogger: SyncRuntimeDiagnosticsLogger
) {
    private val syncMutex = Mutex()

    private val activeListSyncs =
        mutableMapOf<String, Job>()

    suspend fun triggerSync(
        force: Boolean = false
    ): SyncResult {

        return try {

            SyncLog.orchestrator(
                "[Lifecycle] Sync start force=$force"
            )

            if (!force && syncDebounceMs > 0) {
                delay(syncDebounceMs)
            }

            syncMutex.withLock {

                SyncLog.orchestrator(
                    "[Mutex] Enter sync mutex"
                )

                processQueueSequential(force)

                SyncLog.orchestrator(
                    "[Mutex] Exit sync mutex"
                )
            }

            SyncResult.Success

        } catch (e: Exception) {

            RecoveryLog.processError(
                "Sync job crash",
                e
            )

            SyncResult.Failure(e)
        }
    }

    private suspend fun processQueueSequential(
        force: Boolean
    ) {

        SyncLog.queue(
            "[Lifecycle] processQueueSequential ENTER"
        )

        var replayOutcome =
            ReplayOutcome.SUCCESS

        val startedAt =
            System.currentTimeMillis()

        var processedCount = 0

        try {

            while (true) {

                val next =

                    if (force) {

                        changeQueueDao
                            .getOldestPendingChangeIgnoringRetry()

                    } else {

                        changeQueueDao
                            .getOldestPendingChange()
                    }

                SyncLog.queue(
                    "[Fetch] oldestPendingChange=$next"
                )

                if (next == null) {

                    RecoveryLog.policy(
                        "[Queue] No pending changes -> break"
                    )

                    break
                }

                SyncLog.queue(
                    "[Process] op=${next.operation} entity=${next.entityId}"
                )

                SyncLog.queue(
                    "[Before] id=${next.id} state=${next.state} op=${next.operation}"
                )

                val result =
                    processChange(next)

                processedCount++

                when (result) {

                    ChangeProcessingResult.SUCCESS -> {
                        // nothing
                    }

                    ChangeProcessingResult.DEGRADED -> {

                        if (
                            replayOutcome !=
                            ReplayOutcome.FAILURE
                        ) {

                            replayOutcome =
                                ReplayOutcome.DEGRADED_SUCCESS
                        }
                    }
                }

                val after =
                    changeQueueDao.getChangeById(next.id)

                SyncLog.queue(
                    "[After] id=${after?.id} state=${after?.state}"
                )
            }

        } catch (e: Exception) {

        replayOutcome =
            ReplayOutcome.FAILURE

        RecoveryLog.processError(
            "Replay loop failure",
            e
        )

        throw e
    }
        finally {

            if (
                replayOutcome !=
                ReplayOutcome.FAILURE
            ) {

                changeQueueDao.deleteCompleted()

                SyncLog.queue(
                    "[Cleanup] Completed entries deleted"
                )
            }

            val duration =
                System.currentTimeMillis() -
                        startedAt

            telemetry.emit(
                SyncTelemetryEvent.ReplayCompleted(
                    processedCount = processedCount,
                    durationMs = duration,
                    outcome = replayOutcome
                )
            )

            diagnosticsLogger.logSnapshot(
                diagnosticsProvider.build()
            )

            SyncLog.queue(
                "[Lifecycle] processQueueSequential EXIT"
            )

            SyncLog.queue(
                "[Telemetry] ReplayCompleted processed=$processedCount outcome=$replayOutcome durationMs=$duration"
            )
        }
    }


//    fun startSingleListSync(
//        listId: String
//    ) {
//
//        if (activeListSyncs.containsKey(listId)) {
//
//            SyncLog.queue(
//                "[Guard] Sync already running list=$listId"
//            )
//
//            return
//        }
//
//        SyncLog.queue(
//            "[Lifecycle] Start sync list=$listId"
//        )
//
//        val job = appScope.launch {
//
//            launch {
//
//                firestore.observeListById(listId)
//                    .collect { list ->
//
//                        if (list != null) {
//                            listDao.upsert(list)
//                        }
//                    }
//            }
//
//            launch {
//
//                firestore.observeItems(listId)
//                    .collect { remoteItems ->
//
//                        SyncLog.queue(
//                            "[Flow] ITEM emit list=$listId size=${remoteItems.size}"
//                        )
//
//                        remoteItems.forEach { remote ->
//
//                            remoteApplyCoordinator
//                                .applyRemoteItem(remote)
//                        }
//                    }
//            }
//        }
//
//        activeListSyncs[listId] = job
//    }

//    fun stopSingleListSync(
//        listId: String
//    ) {
//
//        val job = activeListSyncs[listId]
//
//        if (job != null) {
//
//            SyncLog.queue(
//                "[Lifecycle] Stop sync list=$listId"
//            )
//
//            job.cancel()
//
//            activeListSyncs.remove(listId)
//
//        } else {
//
//            SyncLog.queue(
//                "[Guard] No active sync list=$listId"
//            )
//        }
//    }

//    suspend fun deleteLocalList(
//        listId: String
//    ) {
//
//        SyncLog.queue(
//            "[Delete] Local list=$listId"
//        )
//
//        itemDao.deleteByListId(listId)
//
//        listDao.deleteById(listId)
//    }

//    fun deleteLocalListAsync(
//        listId: String
//    ) {
//
//        appScope.launch {
//
//            SyncLog.queue(
//                "[DeleteAsync] Local list=$listId"
//            )
//
//            deleteLocalList(listId)
//        }
//    }

    private suspend fun processChange(
        change: ChangeQueueEntity
    ): ChangeProcessingResult {

        val now = System.currentTimeMillis()

        try {

            // ============================================================
            // OWNERSHIP CLAIM
            // ============================================================

            val claimed =
                changeQueueDao.claimProcessingOwnership(
                    id = change.id,
                    timestamp = now
                )

            if (claimed == 0) {

                SyncLog.queue(
                    "[Claim] Already owned id=${change.id}"
                )

                return ChangeProcessingResult.SUCCESS
            }

            // ============================================================
            // ENTITY SWITCH
            // ============================================================

            when (change.entityType) {

                // ============================================================
                // ITEMS
                // ============================================================

                "item" -> {

                    val itemNullable =
                        itemDao.getById(change.entityId)

                    if (
                        itemNullable == null &&
                        change.operation != "DELETE"
                    ) {

                        RecoveryLog.policy(
                            "[MissingLocal] RETRY id=${change.entityId} op=${change.operation}"
                        )

                        val newRetry =
                            change.retryCount + 1

                        if (newRetry >= 5) {

                            RecoveryLog.processError(
                                "FINAL FAIL id=${change.entityId}"
                            )

                            changeQueueDao.updateState(
                                change.id,
                                QueueState.FAILED.name
                            )

                            telemetry.emit(
                                SyncTelemetryEvent.RetryExhausted(
                                    entityId = change.entityId
                                )
                            )

                            diagnosticsLogger.logSnapshot(
                                diagnosticsProvider.build()
                            )

                            return ChangeProcessingResult.DEGRADED
                        }

                        RecoveryLog.policy(
                            "[Retry] id=${change.entityId} retry=$newRetry"
                        )

                        val retryDelayMs =
                            RetryBackoffCalculator.calculate(newRetry)

                        val nextRetryAt =
                            now + retryDelayMs

                        changeQueueDao.updateRetry(
                            id = change.id,
                            state = QueueState.RETRY_WAIT.name,
                            retryCount = newRetry,
                            timestamp = now,
                            nextRetryAt = nextRetryAt
                        )

                        telemetry.emit(
                            SyncTelemetryEvent.RetryScheduled(
                                entityId = change.entityId,
                                retryCount = newRetry
                            )
                        )

                        return ChangeProcessingResult.DEGRADED
                    }

                    // ============================================================
                    // UPDATE PRE-GUARD
                    // ============================================================

                    when (change.operation) {

                        "UPDATE" -> {

                            val item =
                                requireNotNull(itemNullable) {
                                    "UPDATE without local item id=${change.entityId}"
                                }

                            val remote =
                                firestore.getItem(
                                    item.listId,
                                    item.id
                                )

                            if (
                                remote != null &&
                                remote.updatedAt > item.updatedAt
                            ) {

                                SyncLog.orchestrator(
                                    "[Guard] Skip outdated update id=${item.id}"
                                )

                                changeQueueDao.updateState(
                                    change.id,
                                    QueueState.DONE.name
                                )

                                return ChangeProcessingResult.SUCCESS
                            }
                        }
                    }

                    // ============================================================
                    // EXECUTION
                    // ============================================================

                    when (change.operation) {

                        // ============================================================
                        // CREATE
                        // ============================================================

                        "CREATE" -> {

                            val targetId =
                                change.entityId

                            val targetListId =
                                change.listId

                            val localItem =
                                itemDao.getById(targetId)
                                    ?: throw Exception(
                                        "CREATE without local item id=$targetId"
                                    )

                            val remote =
                                firestore.getItem(
                                    targetListId,
                                    targetId
                                )

                            if (remote != null) {

                                SyncLog.orchestrator(
                                    "[Guard] CREATE already exists remotely id=$targetId"
                                )

                                changeQueueDao.updateState(
                                    change.id,
                                    QueueState.DONE.name
                                )

                                return ChangeProcessingResult.SUCCESS
                            }

                            SyncLog.apply(
                                "[Execute] CREATE id=$targetId baseVersion=${change.baseVersion}"
                            )

                            // =====================================================
                            // REAL FIRESTORE WRITE
                            // =====================================================

                            val success =
                                firestore.addItem(
                                    targetListId,
                                    localItem
                                )

                            if (!success) {

                                throw Exception(
                                    "CREATE failed id=$targetId"
                                )
                            }

                            changeQueueDao.updateState(
                                change.id,
                                QueueState.DONE.name
                            )

                            return ChangeProcessingResult.SUCCESS
                        }

                        // ============================================================
                        // UPDATE
                        // ============================================================

                        "UPDATE" -> {

                            val targetId =
                                change.entityId

                            val targetListId =
                                change.listId

                            val localItem =
                                itemDao.getById(targetId)
                                    ?: throw Exception(
                                        "UPDATE without local item id=$targetId"
                                    )

                            val remote =
                                firestore.getItem(
                                    targetListId,
                                    targetId
                                )

                            val remoteVersion =
                                remote?.updatedAt ?: 0L

                            val baseVersion =
                                change.baseVersion

                            // ============================================================
                            // CONFLICT
                            // ============================================================

                            if (
                                remote != null &&
                                remoteVersion != baseVersion
                            ) {

                                SyncLog.conflict(
                                    "[VersionMismatch] id=$targetId base=$baseVersion remote=$remoteVersion"
                                )

                                val resolved =
                                    conflictResolver
                                        .resolveItemConflict(
                                            local = localItem,
                                            remote = remote,
                                            baseVersion = baseVersion
                                        )

                                when (resolved.strategy) {

                                    ConflictStrategy.USE_REMOTE -> {

                                        SyncLog.conflict(
                                            "[ApplyRemote] id=$targetId"
                                        )

                                        remoteApplyCoordinator
                                            .applyRemoteItem(
                                                resolved.resolvedItem!!
                                            )

                                        changeQueueDao.updateState(
                                            change.id,
                                            QueueState.DONE.name
                                        )

                                        return ChangeProcessingResult.SUCCESS
                                    }

                                    ConflictStrategy.USE_LOCAL -> {

                                        SyncLog.conflict(
                                            "[ForceLocal] overwrite id=$targetId"
                                        )

                                        SyncLog.apply(
                                            "[Execute] USE_LOCAL UPDATE id=$targetId baseVersion=${change.baseVersion}"
                                        )

                                        val success =
                                            firestore.updateItem(
                                                targetListId,
                                                localItem
                                            )

                                        if (!success) {

                                            throw Exception(
                                                "UPDATE failed id=${localItem.id}"
                                            )
                                        }

                                        roomRepository
                                            .onSyncWriteSuccess(
                                                localItem.id,
                                                localItem.updatedAt
                                            )

                                        changeQueueDao.updateState(
                                            change.id,
                                            QueueState.DONE.name
                                        )

                                        return ChangeProcessingResult.SUCCESS
                                    }

                                    ConflictStrategy.MERGE -> {

                                        SyncLog.conflict(
                                            "[Merge] id=$targetId"
                                        )

                                        val merged =
                                            resolved.resolvedItem
                                                ?: throw Exception(
                                                    "Merge produced null"
                                                )

                                        SyncLog.apply(
                                            "[Execute] MERGE UPDATE id=$targetId baseVersion=${change.baseVersion}"
                                        )

                                        val success =
                                            firestore.updateItem(
                                                targetListId,
                                                merged
                                            )

                                        if (!success) {

                                            throw Exception(
                                                "MERGE failed id=${merged.id}"
                                            )
                                        }

                                        remoteApplyCoordinator
                                            .applyRemoteItem(merged)

                                        roomRepository
                                            .onSyncWriteSuccess(
                                                merged.id,
                                                merged.updatedAt
                                            )

                                        changeQueueDao.updateState(
                                            change.id,
                                            QueueState.DONE.name
                                        )

                                        return ChangeProcessingResult.SUCCESS
                                    }
                                }
                            }

                            // ============================================================
                            // IDENTICAL WRITE
                            // ============================================================

                            if (
                                remote != null &&
                                remote.updatedAt == localItem.updatedAt
                            ) {

                                SyncLog.orchestrator(
                                    "[Guard] Skip identical write id=$targetId"
                                )

                                changeQueueDao.updateState(
                                    change.id,
                                    QueueState.DONE.name
                                )

                                return ChangeProcessingResult.SUCCESS
                            }

                            // ============================================================
                            // NORMAL UPDATE
                            // ============================================================

                            SyncLog.apply(
                                "[Execute] NORMAL UPDATE id=$targetId baseVersion=${change.baseVersion}"
                            )

                            val success =
                                firestore.updateItem(
                                    targetListId,
                                    localItem
                                )

                            if (!success) {

                                throw Exception(
                                    "UPDATE failed id=${localItem.id}"
                                )
                            }

                            roomRepository.onSyncWriteSuccess(
                                localItem.id,
                                localItem.updatedAt
                            )

                            changeQueueDao.updateState(
                                change.id,
                                QueueState.DONE.name
                            )

                            return ChangeProcessingResult.SUCCESS
                        }

                        // ============================================================
                        // DELETE
                        // ============================================================

                        "DELETE" -> {

                            val targetId =
                                change.entityId

                            val targetListId =
                                change.listId

                            val remote =
                                firestore.getItem(
                                    targetListId,
                                    targetId
                                )

                            if (remote == null) {

                                changeQueueDao.updateState(
                                    change.id,
                                    QueueState.DONE.name
                                )

                                return ChangeProcessingResult.SUCCESS
                            }

                            val tombstone =
                                remote.copy(
                                    deletedAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )

                            SyncLog.apply(
                                "[Execute] DELETE id=$targetId baseVersion=${change.baseVersion}"
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
                                QueueState.DONE.name
                            )

                            return ChangeProcessingResult.SUCCESS
                        }

                        else -> {

                            throw Exception(
                                "Unsupported item operation ${change.operation}"
                            )
                        }
                    }
                }

                // ============================================================
                // LISTS
                // ============================================================

                "list" -> {

                    when (change.operation) {

                        "CREATE" -> {

                            val uid =
                                firebaseAuth?.currentUser?.uid
                                    ?: throw Exception(
                                        "User not authenticated"
                                    )

                            val payload =
                                change.payload
                                    ?: throw Exception(
                                        "Missing payload"
                                    )

                            val list =
                                Gson().fromJson(
                                    payload,
                                    ShoppingListEntity::class.java
                                )

                            val created =
                                firestore.createList(
                                    list,
                                    uid
                                )

                            if (!created) {

                                throw Exception(
                                    "List create failed id=${list.id}"
                                )
                            }

                            firestore.addMembership(
                                uid,
                                list.id
                            )

                            changeQueueDao.updateState(
                                change.id,
                                QueueState.DONE.name
                            )

                            return ChangeProcessingResult.SUCCESS
                        }

                        "DELETE" -> {

                            firestore.softDeleteList(
                                change.listId
                            )

                            changeQueueDao.updateState(
                                change.id,
                                QueueState.DONE.name
                            )

                            return ChangeProcessingResult.SUCCESS
                        }

                        else -> {

                            throw Exception(
                                "Unsupported list operation ${change.operation}"
                            )
                        }
                    }
                }

                // ============================================================
                // MEMBERSHIP
                // ============================================================

                "membership" -> {

                    val userId =
                        requireNotNull(change.payload) {
                            "Membership missing payload"
                        }

                    runCatching {

                        firestore.addUserToList(
                            listId = change.listId,
                            userId = userId
                        )

                    }.getOrElse {

                        throw Exception(
                            "Membership failed",
                            it
                        )
                    }

                    changeQueueDao.updateState(
                        change.id,
                        QueueState.DONE.name
                    )

                    return ChangeProcessingResult.SUCCESS
                }

                // ============================================================
                // INVITE
                // ============================================================

                "invite" -> {

                    val inviteId =
                        requireNotNull(change.payload) {
                            "Invite missing payload"
                        }

                    runCatching {

                        firestore.markInviteConsumed(
                            inviteId
                        )

                    }.getOrElse {

                        throw Exception(
                            "Invite failed",
                            it
                        )
                    }

                    changeQueueDao.updateState(
                        change.id,
                        QueueState.DONE.name
                    )

                    return ChangeProcessingResult.SUCCESS
                }

                else -> {

                    throw Exception(
                        "Unsupported entity type ${change.entityType}"
                    )
                }
            }

        } catch (e: Exception) {

            RecoveryLog.processError(
                "FAILED ${change.entityType} ${change.entityId}",
                e
            )

            val newRetry =
                change.retryCount + 1

            if (newRetry >= 5) {

                changeQueueDao.updateState(
                    change.id,
                    QueueState.FAILED.name
                )

                telemetry.emit(
                    SyncTelemetryEvent.RetryExhausted(
                        entityId = change.entityId
                    )
                )

                diagnosticsLogger.logSnapshot(
                    diagnosticsProvider.build()
                )

                return ChangeProcessingResult.DEGRADED
            }

            val retryDelayMs =
                RetryBackoffCalculator.calculate(
                    newRetry
                )

            val nextRetryAt =
                now + retryDelayMs

            changeQueueDao.updateRetry(
                id = change.id,
                state = QueueState.RETRY_WAIT.name,
                retryCount = newRetry,
                timestamp = now,
                nextRetryAt = nextRetryAt
            )

            telemetry.emit(
                SyncTelemetryEvent.RetryScheduled(
                    entityId = change.entityId,
                    retryCount = newRetry
                )
            )

            return ChangeProcessingResult.DEGRADED
        }
    }

    suspend fun applyRealtimeItem(
        remote: ShoppingItemEntity
    ) {

        remoteApplyCoordinator
            .applyRemoteItem(remote)
    }

    suspend fun awaitIdle() {
        delay(500)
    }
}

object RetryBackoffCalculator {

    fun calculate(
        retryCount: Int
    ): Long {

        return minOf(
            retryCount * 5000L,
            60000L
        )
    }
}