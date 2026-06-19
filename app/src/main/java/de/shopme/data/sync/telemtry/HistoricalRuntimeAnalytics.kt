package de.shopme.data.sync.telemetry

data class HistoricalRuntimeAnalytics(

    val previousStatus:
    RuntimeStatus?,

    val previousIncidents:
    Set<RuntimeIncident> = emptySet(),

    val runtimeRecovered:
    Boolean
)