package de.shopme.testing.system.telemetry

import de.shopme.data.sync.telemetry.SyncTelemetryCollector
import de.shopme.data.sync.telemetry.SyncTelemetryEvent
import de.shopme.data.sync.telemtry.ReplayOutcome
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplayOutcomeTelemetryTest {

    @Test
    fun successfulReplay_updatesSuccessMetrics() {

        val telemetry =
            SyncTelemetryCollector()

        telemetry.emit(
            SyncTelemetryEvent.ReplayCompleted(
                processedCount = 5,
                durationMs = 1000,
                outcome =
                    ReplayOutcome.SUCCESS
            )
        )

        val metrics =
            telemetry.metrics.value

        assertEquals(
            1,
            metrics.totalReplays
        )

        assertEquals(
            1,
            metrics.successfulReplayRuns
        )

        assertEquals(
            0,
            metrics.degradedReplayRuns
        )

        assertEquals(
            0,
            metrics.failedReplayRuns
        )
    }

    @Test
    fun degradedReplay_updatesDegradedMetrics() {

        val telemetry =
            SyncTelemetryCollector()

        telemetry.emit(
            SyncTelemetryEvent.ReplayCompleted(
                processedCount = 5,
                durationMs = 1000,
                outcome =
                    ReplayOutcome.DEGRADED_SUCCESS
            )
        )

        val metrics =
            telemetry.metrics.value

        assertEquals(
            1,
            metrics.totalReplays
        )

        assertEquals(
            0,
            metrics.successfulReplayRuns
        )

        assertEquals(
            1,
            metrics.degradedReplayRuns
        )

        assertEquals(
            0,
            metrics.failedReplayRuns
        )
    }

    @Test
    fun failedReplay_updatesFailureMetrics() {

        val telemetry =
            SyncTelemetryCollector()

        telemetry.emit(
            SyncTelemetryEvent.ReplayCompleted(
                processedCount = 0,
                durationMs = 500,
                outcome =
                    ReplayOutcome.FAILURE
            )
        )

        val metrics =
            telemetry.metrics.value

        assertEquals(
            1,
            metrics.totalReplays
        )

        assertEquals(
            0,
            metrics.successfulReplayRuns
        )

        assertEquals(
            0,
            metrics.degradedReplayRuns
        )

        assertEquals(
            1,
            metrics.failedReplayRuns
        )
    }
}