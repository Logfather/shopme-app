package de.shopme.data.sync.telemetry

class RuntimeReliabilityAnalyzer {

    fun analyze(
        metrics: SyncMetricsSnapshot
    ): RuntimeReliabilitySnapshot {

        val total =
            metrics.totalReplays

        if (total == 0L) {

            return RuntimeReliabilitySnapshot(

                replaySuccessRate = 1.0,

                replayDegradationRate = 0.0,

                replayFailureRate = 0.0,

                averageReplayDurationMs = 0.0,

                averageProcessedEntries = 0.0
            )
        }

        return RuntimeReliabilitySnapshot(

            replaySuccessRate =

                metrics.successfulReplayRuns
                    .toDouble() / total,

            replayDegradationRate =

                metrics.degradedReplayRuns
                    .toDouble() / total,

            replayFailureRate =

                metrics.failedReplayRuns
                    .toDouble() / total,

            averageReplayDurationMs =

                metrics.totalReplayDurationMs
                    .toDouble() / total,

            averageProcessedEntries =

                metrics.totalProcessedEntries
                    .toDouble() / total
        )
    }
}