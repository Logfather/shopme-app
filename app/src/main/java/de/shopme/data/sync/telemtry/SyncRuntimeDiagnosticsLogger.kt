package de.shopme.data.sync.telemetry

import android.util.Log
import de.shopme.data.sync.telemtry.SyncRuntimeDiagnostics

class SyncRuntimeDiagnosticsLogger {

    fun logSnapshot(
        diagnostics: SyncRuntimeDiagnostics
    ) {

        Log.d(
            TAG,
            buildLogMessage(diagnostics)
        )
    }

    internal fun buildLogMessage(
        diagnostics: SyncRuntimeDiagnostics
    ): String {

        return buildString {

            if (
                diagnostics.scenarios
                    .isNotEmpty()
            ) {

                appendLine(
                    "Scenarios=${diagnostics.scenarios}"
                )
            }

            if (
                diagnostics.reasons.isNotEmpty()
            ) {

                appendLine(
                    "Reasons=${diagnostics.reasons}"
                )
            }

            if (
                diagnostics.incidents.incidents
                    .isNotEmpty()
            ) {

                appendLine(
                    "Incidents=${diagnostics.incidents.incidents}"
                )
            }

            if (
                diagnostics.historical.runtimeRecovered
            ) {

                appendLine(
                    "RuntimeRecovered=true"
                )
            }

            appendLine(
                "PreviousStatus=${diagnostics.historical.previousStatus}"
            )

            appendLine(
                "[RuntimeDiagnostics]"
            )

            appendLine(
                "Status=${diagnostics.status}"
            )

            appendLine(
                "Replays=${diagnostics.metrics.totalReplays}"
            )

            appendLine(
                "SuccessfulReplayRuns=${diagnostics.metrics.successfulReplayRuns}"
            )

            appendLine(
                "DegradedReplayRuns=${diagnostics.metrics.degradedReplayRuns}"
            )

            appendLine(
                "FailedReplayRuns=${diagnostics.metrics.failedReplayRuns}"
            )

            appendLine(
                "RetriesScheduled=${diagnostics.metrics.retriesScheduled}"
            )

            appendLine(
                "RetriesExhausted=${diagnostics.metrics.retriesExhausted}"
            )

            appendLine(
                "StaleDiscards=${diagnostics.metrics.staleRemoteDiscards}"
            )

            appendLine(
                "RemoteNewerApplied=${diagnostics.metrics.remoteNewerApplied}"
            )

            appendLine(
                "ReplaySuccessRate=${diagnostics.reliability.replaySuccessRate}"
            )

            appendLine(
                "ReplayDegradationRate=${diagnostics.reliability.replayDegradationRate}"
            )

            appendLine(
                "ReplayFailureRate=${diagnostics.reliability.replayFailureRate}"
            )

            appendLine(
                "AverageReplayDurationMs=${diagnostics.reliability.averageReplayDurationMs}"
            )

            appendLine(
                "AverageProcessedEntries=${diagnostics.reliability.averageProcessedEntries}"
            )

            appendLine(
                "RecentReplaySuccessRate=${diagnostics.trends.recentReplaySuccessRate}"
            )

            appendLine(
                "RecentReplayDegradationRate=${diagnostics.trends.recentReplayDegradationRate}"
            )

            appendLine(
                "RecentReplayFailureRate=${diagnostics.trends.recentReplayFailureRate}"
            )

            appendLine(
                "RecentAverageReplayDurationMs=${diagnostics.trends.recentAverageReplayDurationMs}"
            )

            appendLine(
                "RecentAverageProcessedEntries=${diagnostics.trends.recentAverageProcessedEntries}"
            )

            appendLine(
                "TimelineEvents=${diagnostics.timeline.size}"
            )
        }
    }

    companion object {

        private const val TAG =
            "RUNTIME_DIAGNOSTICS"
    }
}