package de.shopme.domain.sync

import de.shopme.domain.model.SyncStatus

fun String.toSyncStatus(): SyncStatus {
    return when (this) {
        "PENDING" -> SyncStatus.Pending
        "SYNCING" -> SyncStatus.Syncing()
        "FAILED" -> SyncStatus.Failed()
        else -> SyncStatus.Synced
    }
}