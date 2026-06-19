package de.shopme.data.sync.telemetry

import de.shopme.data.sync.telemtry.ReplayOutcome

sealed interface SyncTelemetryEvent {

    data class ReplayStarted(
        val queueDepth: Int,
        val timestamp: Long
    ) : SyncTelemetryEvent

    data class ReplayCompleted(
        val processedCount: Int,
        val durationMs: Long,
        val outcome: ReplayOutcome
    ) : SyncTelemetryEvent

    data class RetryScheduled(
        val entityId: String,
        val retryCount: Int
    ) : SyncTelemetryEvent

    data class RetryExhausted(
        val entityId: String
    ) : SyncTelemetryEvent

    data class StaleRemoteDiscarded(
        val entityId: String
    ) : SyncTelemetryEvent

    data class RemoteNewerApplied(
        val entityId: String
    ) : SyncTelemetryEvent
}