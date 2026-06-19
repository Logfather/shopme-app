package de.shopme.data.sync.telemetry

import de.shopme.data.sync.telemtry.SyncRuntimeDiagnostics

data class PersistedRuntimeSnapshot(

    val timestamp: Long,

    val diagnostics:
    SyncRuntimeDiagnostics
)