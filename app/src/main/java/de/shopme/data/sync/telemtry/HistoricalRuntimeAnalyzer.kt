package de.shopme.data.sync.telemetry

import de.shopme.data.sync.telemtry.SyncRuntimeDiagnostics

class HistoricalRuntimeAnalyzer {

    fun analyze(

        previous:
        PersistedRuntimeSnapshot?,

        current:
        SyncRuntimeDiagnostics
    ): HistoricalRuntimeAnalytics {

        val previousStatus =
            previous
                ?.diagnostics
                ?.status

        val previousIncidents =
            previous
                ?.diagnostics
                ?.incidents
                ?.incidents
                ?: emptySet()

        val runtimeRecovered =

            previousStatus ==
                    RuntimeStatus.CRITICAL &&

                    current.status ==
                    RuntimeStatus.HEALTHY

        return HistoricalRuntimeAnalytics(

            previousStatus =
                previousStatus,

            previousIncidents =
                previousIncidents,

            runtimeRecovered =
                runtimeRecovered
        )
    }
}