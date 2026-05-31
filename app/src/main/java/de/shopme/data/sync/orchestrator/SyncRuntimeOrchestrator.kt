package de.shopme.data.sync.orchestrator

import de.shopme.data.sync.ReplayCompletionNotifier
import de.shopme.data.sync.SyncConstants
import de.shopme.data.sync.SyncScheduler
import de.shopme.data.sync.logging.SyncLog
import de.shopme.data.sync.model.ReplayExecutionState
import de.shopme.data.sync.model.ReplayReason
import de.shopme.data.sync.model.ReplayRequest
import de.shopme.data.sync.util.ReplayIdGenerator

class SyncRuntimeOrchestrator(
    private val syncScheduler: SyncScheduler
) : ReplayCompletionNotifier {

    private var lastReplayTimestamp = 0L

    private var startupReplayActive = false

    private var replayExecutionState =
        ReplayExecutionState.IDLE

    private fun createReplayRequest(
        reason: ReplayReason
    ): ReplayRequest {

        return ReplayRequest(
            replayId = ReplayIdGenerator.newReplayId(),
            reason = reason,
            timestamp = System.currentTimeMillis()
        )
    }

    fun onStartup() {

        SyncLog.orchestrator(
            "Startup replay triggered"
        )

        startupReplayActive = true

        val request =
            createReplayRequest(
                ReplayReason.STARTUP
            )

        SyncLog.orchestrator(
            "Replay requested | id=${request.replayId} | reason=${request.reason}"
        )

        markReplayRunning()

        syncScheduler.enqueueReplay(request)

        markReplayTriggered()
    }

    fun onReconnect() {

        SyncLog.orchestrator(
            "Reconnect detected"
        )

        if (startupReplayActive) {

            SyncLog.orchestrator(
                "Reconnect replay suppressed | startup replay active"
            )

            startupReplayActive = false

            return
        }

        if (isReplayCooldownActive()) {

            SyncLog.orchestrator(
                "Reconnect replay suppressed | cooldown active"
            )

            return
        }

        if (isReplayRunning()) {

            suppressReplayBecauseRunning(
                ReplayReason.RECONNECT
            )

            return
        }

        val request =
            createReplayRequest(
                ReplayReason.RECONNECT
            )

        SyncLog.orchestrator(
            "Replay requested | id=${request.replayId} | reason=${request.reason}"
        )

        markReplayRunning()

        syncScheduler.enqueueReplay(request)

        markReplayTriggered()
    }

    private fun isReplayCooldownActive(): Boolean {

        val now =
            System.currentTimeMillis()

        return now - lastReplayTimestamp <
                SyncConstants.REPLAY_COOLDOWN_MS
    }

    private fun markReplayTriggered() {

        lastReplayTimestamp =
            System.currentTimeMillis()
    }

    private fun markReplayRunning() {

        replayExecutionState =
            ReplayExecutionState.RUNNING

        SyncLog.orchestrator(
            "Replay execution state -> RUNNING"
        )
    }

    private fun markReplayIdle() {

        replayExecutionState =
            ReplayExecutionState.IDLE

        SyncLog.orchestrator(
            "Replay execution state -> IDLE"
        )
    }

    override fun onReplayCompleted() {

        SyncLog.orchestrator(
            "Replay completed"
        )

        markReplayIdle()
    }

    private fun isReplayRunning(): Boolean {

        return replayExecutionState ==
                ReplayExecutionState.RUNNING
    }

    private fun suppressReplayBecauseRunning(
        reason: ReplayReason
    ) {

        SyncLog.orchestrator(
            "Replay suppressed | replay already running | reason=$reason"
        )
    }

    fun onManualReplay() {

        if (isReplayRunning()) {

            suppressReplayBecauseRunning(
                ReplayReason.MANUAL
            )

            return
        }

        val request =
            createReplayRequest(
                ReplayReason.MANUAL
            )

        SyncLog.orchestrator(
            "Replay requested | id=${request.replayId} | reason=${request.reason}"
        )

        markReplayRunning()

        syncScheduler.enqueueReplay(request)
    }
}