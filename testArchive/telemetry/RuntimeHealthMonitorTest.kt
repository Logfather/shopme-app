package de.shopme.testing.system.telemetry

import de.shopme.data.sync.telemetry.RuntimeHealthEvaluator
import de.shopme.data.sync.telemetry.RuntimeHealthMonitor
import de.shopme.data.sync.telemetry.RuntimeStatus
import de.shopme.data.sync.telemetry.SyncTelemetryCollector
import de.shopme.data.sync.telemetry.SyncTelemetryEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class RuntimeHealthMonitorTest {

    @Test
    fun initialState_isHealthy() = runTest {

        val telemetry =
            SyncTelemetryCollector()

        val evaluator =
            RuntimeHealthEvaluator()

        val monitor =
            RuntimeHealthMonitor(
                telemetry = telemetry,
                evaluator = evaluator
            )

        advanceUntilIdle()

        assertEquals(
            RuntimeStatus.HEALTHY,
            monitor.current().status
        )
    }

    @Test
    fun retryStorm_updatesHealthToDegraded() =
        runTest {

            val telemetry =
                SyncTelemetryCollector()

            val evaluator =
                RuntimeHealthEvaluator()

            val monitor =
                RuntimeHealthMonitor(
                    telemetry = telemetry,
                    evaluator = evaluator
                )

            repeat(30) {

                telemetry.emit(
                    SyncTelemetryEvent.RetryScheduled(
                        entityId = "item-$it",
                        retryCount = 1
                    )
                )
            }

            advanceUntilIdle()

            val health =
                monitor.current()

            assertEquals(
                RuntimeStatus.DEGRADED,
                health.status
            )

            assertTrue(
                health.retryStormDetected
            )
        }

    @Test
    fun replayFailure_updatesHealthToCritical() =
        runTest {

            val telemetry =
                SyncTelemetryCollector()

            val evaluator =
                RuntimeHealthEvaluator()

            val monitor =
                RuntimeHealthMonitor(
                    telemetry = telemetry,
                    evaluator = evaluator
                )

            telemetry.emit(
                SyncTelemetryEvent.ReplayStarted(
                    queueDepth = 10,
                    timestamp = 1L
                )
            )

            advanceUntilIdle()

            val health =
                monitor.current()

            assertEquals(
                RuntimeStatus.HEALTHY,
                health.status
            )
        }
}