package de.shopme.data.sync.telemetry

class RuntimeHealthMonitor(

    private val telemetry:
    SyncTelemetryCollector,

    private val evaluator:
    RuntimeHealthEvaluator
) {

    fun current(

        metrics: SyncMetricsSnapshot,

        reliability:
        RuntimeReliabilitySnapshot,

        trends:
        RuntimeTrendSnapshot,

        incidents:
        RuntimeIncidentSnapshot
    ): RuntimeHealthState {

        return evaluator.evaluate(
            metrics = metrics,
            reliability = reliability,
            trends = trends,
            incidents = incidents
        )
    }
}