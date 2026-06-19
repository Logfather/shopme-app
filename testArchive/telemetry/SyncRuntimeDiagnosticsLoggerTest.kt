package de.shopme.testing.system.telemetry

import de.shopme.data.sync.telemetry.RuntimeStatus
import de.shopme.data.sync.telemetry.SyncMetricsSnapshot
import de.shopme.data.sync.telemetry.SyncRuntimeDiagnosticsLogger
import de.shopme.data.sync.telemtry.SyncRuntimeDiagnostics
import kotlin.test.Test
import kotlin.test.assertTrue

class SyncRuntimeDiagnosticsLoggerTest {

    @Test
    fun healthyRuntime_buildsHealthyLogMessage() {

        val diagnostics =
            SyncRuntimeDiagnostics(

                status =
                    RuntimeStatus.HEALTHY,

                reasons = emptyList(),

                scenarios = emptySet(),

                metrics =
                    SyncMetricsSnapshot(),

                summary = "Healthy"
            )

        val logger =
            SyncRuntimeDiagnosticsLogger()

        val message =
            logger.buildLogMessage(
                diagnostics
            )

        assertTrue(
            message.contains(
                "Status=HEALTHY"
            )
        )
    }

    @Test
    fun degradedRuntime_includesReasons() {

        val diagnostics =
            SyncRuntimeDiagnostics(

                status =
                    RuntimeStatus.HEALTHY,

                reasons = emptyList(),

                scenarios = emptySet(),

                metrics =
                    SyncMetricsSnapshot(),

                summary = "Healthy"
            )

        val logger =
            SyncRuntimeDiagnosticsLogger()

        val message =
            logger.buildLogMessage(
                diagnostics
            )

        assertTrue(
            message.contains(
                "Status=DEGRADED"
            )
        )

        assertTrue(
            message.contains(
                "High retry volume detected"
            )
        )

        assertTrue(
            message.contains(
                "RetriesScheduled=31"
            )
        )
    }
}