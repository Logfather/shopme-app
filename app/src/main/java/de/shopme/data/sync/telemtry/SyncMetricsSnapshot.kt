package de.shopme.data.sync.telemetry

data class SyncMetricsSnapshot(

    val totalReplays: Long = 0,

    val totalReplayDurationMs: Long = 0,

    val successfulReplays: Long = 0,

    val totalProcessedEntries: Long = 0,

    val retriesScheduled: Long = 0,

    val retriesExhausted: Long = 0,

    val staleRemoteDiscards: Long = 0,

    val remoteNewerApplied: Long = 0,

    val successfulReplayRuns: Int = 0,

    val degradedReplayRuns: Int = 0,

    val failedReplayRuns: Int = 0
)