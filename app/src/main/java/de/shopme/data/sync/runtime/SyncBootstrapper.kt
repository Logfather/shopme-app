package de.shopme.data.sync.runtime

import de.shopme.data.sync.FirestoreListener
import de.shopme.data.sync.QueueState
import de.shopme.data.sync.SyncCoordinator
import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.data.sync.logging.SyncLog
import de.shopme.data.sync.queue.ChangeQueueDao
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SyncBootstrapper(
    val syncCoordinator: SyncCoordinator,
    private val changeQueueDao: ChangeQueueDao,
    private val runtimeStateHolder: SyncRuntimeStateHolder,
    private val listenerRegistry: ListenerActivationRegistry,
    private val firestoreListener: FirestoreListener
) {

    private val startupMutex = Mutex()

    suspend fun startUserRuntime(
        uid: String
    ) {

        startupMutex.withLock {

            RuntimeLog.sync(
                "Bootstrap start uid=$uid"
            )

            try {

                runtimeStateHolder.setState(
                    SyncRuntimeState.RECOVERING
                )

                recoverRuntime()

                runtimeStateHolder.setState(
                    SyncRuntimeState.REPLAYING
                )

                replayPendingQueue()

                runtimeStateHolder.setState(
                    SyncRuntimeState.ATTACHING_LISTENERS
                )

                startMembershipDiscovery(uid)

                runtimeStateHolder.setState(
                    SyncRuntimeState.REALTIME_ACTIVE
                )

                RuntimeLog.sync(
                    "Bootstrap completed uid=$uid"
                )

            } catch (e: Exception) {

                runtimeStateHolder.setState(
                    SyncRuntimeState.ERROR
                )

                RuntimeLog.fatal(
                    "Bootstrap failed uid=$uid",
                    e
                )

                throw e
            }
        }
    }

    private fun startMembershipDiscovery(
        uid: String
    ) {

        RuntimeLog.sync(
            "Membership discovery start uid=$uid"
        )

        firestoreListener.startListSync(uid)
    }

    suspend fun recoverRuntime() {

        RuntimeLog.sync(
            "Recovery start"
        )

        changeQueueDao
            .recoverInterruptedProcessing()

        RuntimeLog.sync(
            "Recovery complete"
        )
    }

    private val replayMutex = Mutex()

    suspend fun replayPendingQueue() {

        replayMutex.withLock {

            RuntimeLog.sync(
                "Replay start"
            )

            syncCoordinator.triggerSync(
                force = true
            )

            waitUntilReplaySettled()

            RuntimeLog.sync(
                "Replay complete"
            )
        }
    }

    suspend fun activateList(
        listId: String
    ) {

        val state =
            runtimeStateHolder.currentState()

        if (
            state != SyncRuntimeState.ATTACHING_LISTENERS &&
            state != SyncRuntimeState.REALTIME_ACTIVE
        ) {

            SyncLog.realtime(
                "[BootstrapGuard] Delayed activation list=$listId state=$state"
            )

            listenerRegistry
                .requestActivation(listId)

            return
        }

        if (
            listenerRegistry
                .isAlreadyActive(listId)
        ) {

            SyncLog.realtime(
                "[BootstrapGuard] Already active list=$listId"
            )

            return
        }

        SyncLog.realtime(
            "[Bootstrap] Activate list=$listId"
        )

        firestoreListener
            .startItemSync(listId)

        listenerRegistry
            .markActive(listId)
    }

    suspend fun deactivateList(
        listId: String
    ) {

        SyncLog.realtime(
            "[Bootstrap] Deactivate list=$listId"
        )

        firestoreListener
            .stopItemSync(listId)

        listenerRegistry
            .markInactive(listId)
    }

    private suspend fun waitUntilReplaySettled() {

        RuntimeLog.sync(
            "Wait replay settled"
        )

        while (true) {

            val pending =
                changeQueueDao.getPendingChanges()

            val hasProcessing =
                changeQueueDao
                    .getAllChanges()
                    .any {
                        it.state == QueueState.PROCESSING.name
                    }

            if (
                pending.isEmpty() &&
                !hasProcessing
            ) {
                break
            }

            delay(250)
        }

        RuntimeLog.sync(
            "Replay settled"
        )
    }
}