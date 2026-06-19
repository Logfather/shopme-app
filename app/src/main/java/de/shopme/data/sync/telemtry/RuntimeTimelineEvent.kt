package de.shopme.data.sync.telemetry

data class RuntimeTimelineEvent(

    val timestamp: Long,

    val status: RuntimeStatus,

    val incidents:
    Set<RuntimeIncident>,

    val scenarios:
    Set<RuntimeDiagnosticScenario>,

    val summary: String
)