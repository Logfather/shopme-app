package de.shopme.data.sync.telemetry

class RuntimeIncidentDetector {

    fun detect(

        reliability:
        RuntimeReliabilitySnapshot,

        trends:
        RuntimeTrendSnapshot
    ): RuntimeIncidentSnapshot {

        val incidents =
            mutableSetOf<RuntimeIncident>()

        // =====================================================
        // Replay Degradation Spike
        // =====================================================

        if (
            trends.recentReplayDegradationRate >= 0.3
        ) {

            incidents +=
                RuntimeIncident
                    .REPLAY_DEGRADATION_SPIKE
        }

        // =====================================================
        // Replay Failure Burst
        // =====================================================

        if (
            trends.recentReplayFailureRate > 0.0
        ) {

            incidents +=
                RuntimeIncident
                    .REPLAY_FAILURE_BURST
        }

        // =====================================================
        // Replay Slowdown
        // =====================================================

        if (
            reliability.averageReplayDurationMs > 0 &&
            trends.recentAverageReplayDurationMs >

            reliability.averageReplayDurationMs * 2
        ) {

            incidents +=
                RuntimeIncident
                    .REPLAY_SLOWDOWN
        }

        return RuntimeIncidentSnapshot(
            incidents = incidents
        )
    }
}