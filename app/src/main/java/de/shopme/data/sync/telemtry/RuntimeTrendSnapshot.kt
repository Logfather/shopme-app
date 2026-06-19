package de.shopme.data.sync.telemetry

data class RuntimeTrendSnapshot(

    val recentReplaySuccessRate: Double,

    val recentReplayDegradationRate: Double,

    val recentReplayFailureRate: Double,

    val recentAverageReplayDurationMs: Double,

    val recentAverageProcessedEntries: Double
)