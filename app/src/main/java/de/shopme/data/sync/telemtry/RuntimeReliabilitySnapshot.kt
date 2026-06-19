package de.shopme.data.sync.telemetry

data class RuntimeReliabilitySnapshot(

    val replaySuccessRate: Double,

    val replayDegradationRate: Double,

    val replayFailureRate: Double,

    val averageReplayDurationMs: Double,

    val averageProcessedEntries: Double
)