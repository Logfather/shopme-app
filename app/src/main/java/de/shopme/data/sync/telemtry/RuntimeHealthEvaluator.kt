package de.shopme.data.sync.telemetry


// NOTE:
// Historical failures currently keep runtime degraded.
// Later phases may transition this to trend-based health.


class RuntimeHealthEvaluator {

    fun evaluate(

        metrics: SyncMetricsSnapshot,

        reliability: RuntimeReliabilitySnapshot,

        trends: RuntimeTrendSnapshot,

        incidents: RuntimeIncidentSnapshot

    ): RuntimeHealthState {

        val reasons =
            mutableListOf<String>()

        var retryStormDetected = false

        var staleDiscardFloodDetected = false

        var replayFailureDetected = false

        var criticalDetected = false

        var recoveringDetected = false

        val scenarios =
            mutableSetOf<RuntimeDiagnosticScenario>()

        // =====================================================
        // Retry Storm
        // =====================================================

        if (metrics.retriesScheduled >= 25) {

            retryStormDetected = true

            reasons +=
                "High retry volume detected"

            scenarios +=
                RuntimeDiagnosticScenario.RETRY_STORM
        }


        // =====================================================
        // Stale Flood
        // =====================================================

        if (metrics.staleRemoteDiscards >= 50) {

            staleDiscardFloodDetected = true

            reasons +=
                "High stale discard volume detected"

            scenarios +=
                RuntimeDiagnosticScenario.STALE_DISCARD_FLOOD
        }

        // =====================================================
        // Retry Exhaustion
        // =====================================================

        if (metrics.retriesExhausted > 0) {

            replayFailureDetected = true

            reasons +=
                "Replay failures detected"

            scenarios +=
                RuntimeDiagnosticScenario.REPLAY_FAILURES
        }

        // =====================================================
        // FAILURE BURST
        // =====================================================

        if (
            RuntimeIncident
                .REPLAY_FAILURE_BURST
            in incidents.incidents
        ) {

            criticalDetected = true

            reasons +=
                "Replay failure burst detected"
        }

        // =====================================================
        // MULTI INCIDENT CRITICALITY
        // =====================================================


        if (
            incidents.incidents.size >= 2
        ) {

            criticalDetected = true

            reasons +=
                "Multiple runtime incidents detected"
        }

        // =====================================================
        // RECOVERING
        // =====================================================

        if (

            reliability.replayDegradationRate > 0.2 &&

            trends.recentReplayDegradationRate <
            0.1 &&

            trends.recentReplayFailureRate ==
            0.0
        ) {

            recoveringDetected = true

            reasons +=
                "Runtime recovering"
        }

        val status =
            when {

                criticalDetected -> {

                    RuntimeStatus.CRITICAL
                }

                recoveringDetected -> {

                    RuntimeStatus.RECOVERING
                }

                retryStormDetected ||
                        staleDiscardFloodDetected ||
                        replayFailureDetected -> {

                    RuntimeStatus.DEGRADED
                }

                else -> {

                    RuntimeStatus.HEALTHY
                }
            }

        return RuntimeHealthState(

            status = status,

            reasons = reasons,

            scenarios = scenarios,

            retryStormDetected =
                retryStormDetected,

            staleDiscardFloodDetected =
                staleDiscardFloodDetected,

            replayFailureDetected =
                replayFailureDetected
        )
    }
}