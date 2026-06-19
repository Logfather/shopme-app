package de.shopme.data.sync.telemetry

data class RuntimeHealthState(

    val status: RuntimeStatus,

    val reasons: List<String> = emptyList(),

    val retryStormDetected: Boolean = false,

    val staleDiscardFloodDetected: Boolean = false,

    val replayFailureDetected: Boolean = false,

    val scenarios: Set<RuntimeDiagnosticScenario>
)