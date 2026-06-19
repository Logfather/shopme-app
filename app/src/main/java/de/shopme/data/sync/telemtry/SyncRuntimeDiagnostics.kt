package de.shopme.data.sync.telemtry

import de.shopme.data.sync.telemetry.HistoricalRuntimeAnalytics
import de.shopme.data.sync.telemetry.RuntimeDiagnosticScenario
import de.shopme.data.sync.telemetry.RuntimeIncidentSnapshot
import de.shopme.data.sync.telemetry.RuntimeReliabilitySnapshot
import de.shopme.data.sync.telemetry.RuntimeStatus
import de.shopme.data.sync.telemetry.RuntimeTimelineEvent
import de.shopme.data.sync.telemetry.RuntimeTrendSnapshot
import de.shopme.data.sync.telemetry.SyncMetricsSnapshot

data class SyncRuntimeDiagnostics(

    val status: RuntimeStatus,

    val reasons: List<String>,

    val metrics: SyncMetricsSnapshot,

    val summary: String,

    val scenarios: Set<RuntimeDiagnosticScenario>,

    val reliability: RuntimeReliabilitySnapshot,

    val trends: RuntimeTrendSnapshot,

    val incidents: RuntimeIncidentSnapshot,

    val historical: HistoricalRuntimeAnalytics,

    val timeline: List<RuntimeTimelineEvent>
)