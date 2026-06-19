package de.shopme.data.sync.telemetry

class InMemoryRuntimeSnapshotStore :
    RuntimeSnapshotStore {

    private var snapshot:
            PersistedRuntimeSnapshot? = null

    override suspend fun save(

        snapshot:
        PersistedRuntimeSnapshot
    ) {

        this.snapshot = snapshot
    }

    override suspend fun load():
            PersistedRuntimeSnapshot? {

        return snapshot
    }
}