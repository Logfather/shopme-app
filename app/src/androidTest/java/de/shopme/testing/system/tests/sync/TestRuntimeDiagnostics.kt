package de.shopme.testing.system.tests.sync

import de.shopme.data.sync.telemetry.HistoricalRuntimeAnalyzer
import de.shopme.data.sync.telemetry.InMemoryRuntimeSnapshotStore
import de.shopme.data.sync.telemetry.RuntimeHealthEvaluator
import de.shopme.data.sync.telemetry.RuntimeHealthMonitor
import de.shopme.data.sync.telemetry.RuntimeIncidentDetector
import de.shopme.data.sync.telemetry.RuntimeIncidentTimeline
import de.shopme.data.sync.telemetry.RuntimeReliabilityAnalyzer
import de.shopme.data.sync.telemetry.SyncRuntimeDiagnosticsLogger
import de.shopme.data.sync.telemetry.SyncRuntimeDiagnosticsProvider
import de.shopme.data.sync.telemetry.SyncTelemetryCollector

object TestRuntimeDiagnostics {

    fun provider(
        telemetry: SyncTelemetryCollector
    ) =

        SyncRuntimeDiagnosticsProvider(

            telemetry = telemetry,

            healthMonitor =

                RuntimeHealthMonitor(

                    telemetry = telemetry,

                    evaluator =

                        RuntimeHealthEvaluator()

                ),

            reliabilityAnalyzer =

                RuntimeReliabilityAnalyzer(),

            incidentDetector =

                RuntimeIncidentDetector(),

            snapshotStore =

                InMemoryRuntimeSnapshotStore(),

            historicalAnalyzer =

                HistoricalRuntimeAnalyzer(),

            timeline =

                RuntimeIncidentTimeline()

        )

    fun logger() =

        SyncRuntimeDiagnosticsLogger()

}