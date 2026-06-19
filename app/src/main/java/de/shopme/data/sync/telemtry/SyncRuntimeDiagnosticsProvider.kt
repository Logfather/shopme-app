package de.shopme.data.sync.telemetry

import de.shopme.data.sync.telemtry.SyncRuntimeDiagnostics

class SyncRuntimeDiagnosticsProvider(

    private val telemetry:
    SyncTelemetryCollector,

    private val healthMonitor:
    RuntimeHealthMonitor,

    private val reliabilityAnalyzer:
    RuntimeReliabilityAnalyzer,

    private val incidentDetector:
    RuntimeIncidentDetector,

    private val snapshotStore:
    RuntimeSnapshotStore,

    private val historicalAnalyzer:
    HistoricalRuntimeAnalyzer,

    private val timeline:
    RuntimeIncidentTimeline

) {

    suspend fun build():
            SyncRuntimeDiagnostics {

        val metrics =
            telemetry.metrics.value

        val reliability =
            reliabilityAnalyzer.analyze(metrics)

        val trends =
            telemetry.currentTrends()

        val incidents =
            incidentDetector.detect(
                reliability = reliability,
                trends = trends
            )

        val health =
            healthMonitor.current(
                metrics = metrics,
                reliability = reliability,
                trends = trends,
                incidents = incidents
            )

        // =====================================================
        // PROVISIONAL DIAGNOSTICS
        // =====================================================

        val provisional =
            SyncRuntimeDiagnostics(

                status = health.status,

                reasons = health.reasons,

                scenarios = health.scenarios,

                metrics = metrics,

                reliability = reliability,

                trends = trends,

                incidents = incidents,

                historical =
                    HistoricalRuntimeAnalytics(
                        previousStatus = null,
                        previousIncidents = emptySet(),
                        runtimeRecovered = false
                    ),

                timeline = emptyList(),

                summary = ""
            )

        // =====================================================
        // HISTORICAL ANALYTICS
        // =====================================================

        val previous =
            snapshotStore.load()

        val historical =
            historicalAnalyzer.analyze(
                previous = previous,
                current = provisional
            )

        // =====================================================
        // FINAL DIAGNOSTICS
        // =====================================================

        val diagnostics =
            provisional.copy(
                historical = historical
            )

        // =====================================================
        // PERSIST SNAPSHOT
        // =====================================================

        snapshotStore.save(

            PersistedRuntimeSnapshot(

                timestamp =
                    System.currentTimeMillis(),

                diagnostics = diagnostics
            )
        )

        timeline.record(

            RuntimeTimelineEvent(

                timestamp =
                    System.currentTimeMillis(),

                status =
                    diagnostics.status,

                incidents =
                    diagnostics.incidents.incidents,

                scenarios =
                    diagnostics.scenarios,

                summary =
                    diagnostics.summary
            )
        )

        val finalDiagnostics =
            diagnostics.copy(

                timeline =
                    timeline.snapshot(),

                summary =
                    buildSummary(
                        health = health,
                        metrics = metrics,
                        reliability = reliability,
                        trends = trends,
                        incidents = incidents,
                        historical = historical
                    )
            )

        return finalDiagnostics
    }

    private fun buildSummary(

        health: RuntimeHealthState,

        metrics: SyncMetricsSnapshot,

        reliability:
        RuntimeReliabilitySnapshot,

        trends:
        RuntimeTrendSnapshot,

        incidents:
        RuntimeIncidentSnapshot,

        historical:
        HistoricalRuntimeAnalytics

    ): String {

        return buildString {

            appendLine(
                "Runtime Status: ${health.status}"
            )

            if (
                health.scenarios.isNotEmpty()
            ) {

                appendLine()

                appendLine(
                    "Scenarios:"
                )

                health.scenarios.forEach {

                    appendLine(
                        "- $it"
                    )
                }
            }

            if (health.reasons.isNotEmpty()) {

                appendLine()

                appendLine("Reasons:")

                health.reasons.forEach {

                    appendLine("- $it")
                }
            }

            appendLine()

            appendLine("Metrics:")

            appendLine(
                "- Replays: ${metrics.totalReplays}"
            )

            appendLine(
                "- Successful Replays: ${metrics.successfulReplays}"
            )

            appendLine(
                "- Retries Scheduled: ${metrics.retriesScheduled}"
            )

            appendLine(
                "- Retries Exhausted: ${metrics.retriesExhausted}"
            )

            appendLine(
                "- Stale Discards: ${metrics.staleRemoteDiscards}"
            )

            appendLine(
                "- Remote Newer Applied: ${metrics.remoteNewerApplied}"
            )
            appendLine()

            appendLine("Reliability:")

            appendLine(
                "- Replay Success Rate: ${reliability.replaySuccessRate}"
            )

            appendLine(
                "- Replay Degradation Rate: ${reliability.replayDegradationRate}"
            )

            appendLine(
                "- Replay Failure Rate: ${reliability.replayFailureRate}"
            )

            appendLine(
                "- Average Replay Duration: ${reliability.averageReplayDurationMs}"
            )

            appendLine()

            appendLine("Recent Trends:")

            appendLine(
                "- Recent Replay Success Rate: ${trends.recentReplaySuccessRate}"
            )

            appendLine(
                "- Recent Replay Degradation Rate: ${trends.recentReplayDegradationRate}"
            )

            appendLine(
                "- Recent Replay Failure Rate: ${trends.recentReplayFailureRate}"
            )

            appendLine(
                "- Recent Average Replay Duration: ${trends.recentAverageReplayDurationMs}"
            )

            appendLine()

            appendLine("Historical:")

            appendLine(
                "- Previous Status: ${historical.previousStatus}"
            )

            appendLine(
                "- Runtime Recovered: ${historical.runtimeRecovered}"
            )

            if (
                incidents.incidents.isNotEmpty()
            ) {

                appendLine()

                appendLine("Incidents:")

                incidents.incidents.forEach {

                    appendLine("- $it")
                }
            }
        }
    }
}