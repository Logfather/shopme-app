package de.shopme.data.sync.telemetry

data class RuntimeIncidentSnapshot(

    val incidents:
    Set<RuntimeIncident> = emptySet()
)