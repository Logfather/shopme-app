package de.shopme.testing.system.telemetry

import de.shopme.data.sync.telemetry.SyncTelemetryCollector
import de.shopme.data.sync.telemetry.SyncTelemetryEvent
import de.shopme.data.sync.telemtry.ReplayOutcome
import org.junit.Test
import kotlin.test.assertEquals

class SyncTelemetryCollectorTest {

    @Test
    fun replayCompleted_updatesReplayMetrics() {

        val telemetry = SyncTelemetryCollector()

        telemetry.emit(
            SyncTelemetryEvent.ReplayCompleted(
                processedCount = 5,
                durationMs = 1200,
                outcome =
                    ReplayOutcome.SUCCESS
            )
        )

        val metrics = telemetry.metrics.value

        assertEquals(
            1,
            metrics.totalReplays
        )

        assertEquals(
            1,
            metrics.successfulReplays
        )

        assertEquals(
            1200,
            metrics.totalReplayDurationMs
        )

        assertEquals(
            5,
            metrics.totalProcessedEntries
        )
    }

    @Test
    fun multipleReplayEvents_aggregateCorrectly() {

        val telemetry = SyncTelemetryCollector()

        telemetry.emit(
            SyncTelemetryEvent.ReplayCompleted(
                processedCount = 5,
                durationMs = 1200,
                outcome =
                    ReplayOutcome.SUCCESS
            )
        )

        telemetry.emit(
            SyncTelemetryEvent.ReplayCompleted(
                processedCount = 5,
                durationMs = 1200,
                outcome =
                    ReplayOutcome.SUCCESS
            )
        )

        val metrics = telemetry.metrics.value

        assertEquals(2, metrics.totalReplays)

        assertEquals(300, metrics.totalReplayDurationMs)

        assertEquals(5, metrics.totalProcessedEntries)
    }

    @Test
    fun retryScheduled_incrementsCounter() {

        val telemetry = SyncTelemetryCollector()

        telemetry.emit(
            SyncTelemetryEvent.RetryScheduled(
                entityId = "item-1",
                retryCount = 1
            )
        )

        val metrics = telemetry.metrics.value

        assertEquals(1, metrics.retriesScheduled)
    }

    @Test
    fun retryExhausted_incrementsCounter() {

        val telemetry = SyncTelemetryCollector()

        telemetry.emit(
            SyncTelemetryEvent.RetryExhausted(
                entityId = "item-1"
            )
        )

        val metrics = telemetry.metrics.value

        assertEquals(1, metrics.retriesExhausted)
    }

    @Test
    fun staleRemoteDiscarded_incrementsCounter() {

        val telemetry = SyncTelemetryCollector()

        telemetry.emit(
            SyncTelemetryEvent.StaleRemoteDiscarded(
                entityId = "item-1"
            )
        )

        val metrics = telemetry.metrics.value

        assertEquals(1, metrics.staleRemoteDiscards)
    }

    @Test
    fun remoteNewerApplied_incrementsCounter() {

        val telemetry = SyncTelemetryCollector()

        telemetry.emit(
            SyncTelemetryEvent.RemoteNewerApplied(
                entityId = "item-1"
            )
        )

        val metrics = telemetry.metrics.value

        assertEquals(1, metrics.remoteNewerApplied)
    }
}