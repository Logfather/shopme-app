package de.shopme.testing.system.telemetry

import de.shopme.data.sync.telemetry.RuntimeHealthEvaluator
import de.shopme.data.sync.telemetry.RuntimeHealthMonitor
import de.shopme.data.sync.telemetry.RuntimeStatus
import de.shopme.data.sync.telemetry.SyncRuntimeDiagnosticsProvider
import de.shopme.data.sync.telemetry.SyncTelemetryCollector
import de.shopme.data.sync.telemetry.SyncTelemetryEvent
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncRuntimeDiagnosticsProviderTest {

    @Test
    fun healthyRuntime_buildsHealthyDiagnostics() =
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

            val provider =
                SyncRuntimeDiagnosticsProvider(
                    telemetry = telemetry,
                    healthMonitor = monitor
                )

            advanceUntilIdle()

            val diagnostics =
                provider.build()

            assertEquals(
                RuntimeStatus.HEALTHY,
                diagnostics.status
            )

            assertTrue(
                diagnostics.summary.contains(
                    "Runtime Status: HEALTHY"
                )
            )

        }

    @Test
    fun retryStorm_buildsDegradedDiagnostics() =
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

            // WICHTIG:
            // reactive propagation abschließen lassen
            advanceUntilIdle()

            val provider =
                SyncRuntimeDiagnosticsProvider(
                    telemetry = telemetry,
                    healthMonitor = monitor
                )

            val diagnostics =
                provider.build()

            assertEquals(
                RuntimeStatus.DEGRADED,
                diagnostics.status
            )

            assertTrue(
                diagnostics.summary.contains(
                    "High retry volume detected"
                )
            )

            assertTrue(
                diagnostics.summary.contains(
                    "Retries Scheduled: 30"
                )
            )
        }


}