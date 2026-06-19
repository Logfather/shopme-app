package de.shopme.testing.system.telemetry

import de.shopme.data.sync.telemetry.RuntimeReliabilityAnalyzer
import de.shopme.data.sync.telemetry.SyncMetricsSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals

class RuntimeReliabilityAnalyzerTest {

    @Test
    fun emptyMetrics_returnsHealthyDefaults() {

        val analyzer =
            RuntimeReliabilityAnalyzer()

        val result =
            analyzer.analyze(
                SyncMetricsSnapshot()
            )

        assertEquals(
            1.0,
            result.replaySuccessRate,
            0.001
        )

        assertEquals(
            0.0,
            result.replayDegradationRate,
            0.001
        )

        assertEquals(
            0.0,
            result.replayFailureRate,
            0.001
        )

        assertEquals(
            0.0,
            result.averageReplayDurationMs,
            0.001
        )

        assertEquals(
            0.0,
            result.averageProcessedEntries,
            0.001
        )
    }

    @Test
    fun replayRatios_areCalculatedCorrectly() {

        val analyzer =
            RuntimeReliabilityAnalyzer()

        val metrics =
            SyncMetricsSnapshot(

                totalReplays = 10,

                successfulReplayRuns = 7,

                degradedReplayRuns = 2,

                failedReplayRuns = 1,

                totalReplayDurationMs = 5000,

                totalProcessedEntries = 100
            )

        val result =
            analyzer.analyze(metrics)

        assertEquals(
            0.7,
            result.replaySuccessRate,
            0.001
        )

        assertEquals(
            0.2,
            result.replayDegradationRate,
            0.001
        )

        assertEquals(
            0.1,
            result.replayFailureRate,
            0.001
        )

        assertEquals(
            500.0,
            result.averageReplayDurationMs,
            0.001
        )

        assertEquals(
            10.0,
            result.averageProcessedEntries,
            0.001
        )
    }


}