package de.shopme.testing.system.telemetry

import de.shopme.data.sync.telemetry.RuntimeHealthEvaluator
import de.shopme.data.sync.telemetry.RuntimeStatus
import de.shopme.data.sync.telemetry.SyncMetricsSnapshot
import org.junit.Assert.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

class RuntimeHealthEvaluatorTest {

    @Test
    fun healthyMetrics_returnsHealthy() {

        val evaluator =
            RuntimeHealthEvaluator()

        val metrics =
            SyncMetricsSnapshot(
                totalReplays = 10,
                successfulReplays = 10,
                retriesScheduled = 2,
                staleRemoteDiscards = 1
            )

        val result =
            evaluator.evaluate(metrics)

        assertEquals(
            RuntimeStatus.HEALTHY,
            result.status
        )
    }

    @Test
    fun retryStorm_returnsDegraded() {

        val evaluator =
            RuntimeHealthEvaluator()

        val metrics =
            SyncMetricsSnapshot(
                retriesScheduled = 50
            )

        val result =
            evaluator.evaluate(metrics)

        assertEquals(
            RuntimeStatus.DEGRADED,
            result.status
        )

        assertTrue(
            result.retryStormDetected
        )
    }

    @Test
    fun staleFlood_returnsDegraded() {

        val evaluator =
            RuntimeHealthEvaluator()

        val metrics =
            SyncMetricsSnapshot(
                staleRemoteDiscards = 100
            )

        val result =
            evaluator.evaluate(metrics)

        assertEquals(
            RuntimeStatus.DEGRADED,
            result.status
        )

        assertTrue(
            result.staleDiscardFloodDetected
        )
    }

    @Test
    fun replayFailure_returnsCritical() {

        val evaluator =
            RuntimeHealthEvaluator()

        val metrics =
            SyncMetricsSnapshot(
                totalReplays = 5,
                successfulReplays = 0
            )

        val result =
            evaluator.evaluate(metrics)

        assertEquals(
            RuntimeStatus.CRITICAL,
            result.status
        )

        assertTrue(
            result.replayFailureDetected
        )
    }

}