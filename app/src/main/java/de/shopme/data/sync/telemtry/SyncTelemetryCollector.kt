package de.shopme.data.sync.telemetry

import de.shopme.data.sync.telemtry.ReplayOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SyncTelemetryCollector {

    private val _metrics =
        MutableStateFlow(SyncMetricsSnapshot())

    private val trendAnalyzer =
        RuntimeTrendAnalyzer()

    val metrics: StateFlow<SyncMetricsSnapshot> =
        _metrics.asStateFlow()

    val trends =
        trendAnalyzer

    fun currentTrends():
            RuntimeTrendSnapshot {

        return trendAnalyzer.snapshot()
    }

    fun emit(event: SyncTelemetryEvent) {

        _metrics.update { current ->

            when (event) {

                is SyncTelemetryEvent.ReplayStarted -> {
                    current
                }

                is SyncTelemetryEvent.ReplayCompleted -> {

                    trendAnalyzer.recordReplay(

                        outcome = event.outcome,

                        durationMs = event.durationMs,

                        processedCount = event.processedCount
                    )

                    current.copy(

                        totalReplays =
                            current.totalReplays + 1,

                        successfulReplayRuns =

                            current.successfulReplayRuns +

                                    if (
                                        event.outcome ==
                                        ReplayOutcome.SUCCESS
                                    ) {
                                        1
                                    } else {
                                        0
                                    },

                        degradedReplayRuns =

                            current.degradedReplayRuns +

                                    if (
                                        event.outcome ==
                                        ReplayOutcome.DEGRADED_SUCCESS
                                    ) {
                                        1
                                    } else {
                                        0
                                    },

                        failedReplayRuns =

                            current.failedReplayRuns +

                                    if (
                                        event.outcome ==
                                        ReplayOutcome.FAILURE
                                    ) {
                                        1
                                    } else {
                                        0
                                    },

                        totalReplayDurationMs =
                            current.totalReplayDurationMs +
                                    event.durationMs,

                        totalProcessedEntries =
                            current.totalProcessedEntries +
                                    event.processedCount
                    )
                }

                is SyncTelemetryEvent.RetryScheduled -> {
                    current.copy(
                        retriesScheduled =
                            current.retriesScheduled + 1
                    )
                }

                is SyncTelemetryEvent.RetryExhausted -> {
                    current.copy(
                        retriesExhausted =
                            current.retriesExhausted + 1
                    )
                }

                is SyncTelemetryEvent.StaleRemoteDiscarded -> {
                    current.copy(
                        staleRemoteDiscards =
                            current.staleRemoteDiscards + 1
                    )
                }

                is SyncTelemetryEvent.RemoteNewerApplied -> {
                    current.copy(
                        remoteNewerApplied =
                            current.remoteNewerApplied + 1
                    )
                }
            }
        }
    }
}