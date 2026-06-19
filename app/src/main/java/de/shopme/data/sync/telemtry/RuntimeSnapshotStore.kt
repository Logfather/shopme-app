package de.shopme.data.sync.telemetry

interface RuntimeSnapshotStore {

    suspend fun save(

        snapshot:
        PersistedRuntimeSnapshot
    )

    suspend fun load():
            PersistedRuntimeSnapshot?
}