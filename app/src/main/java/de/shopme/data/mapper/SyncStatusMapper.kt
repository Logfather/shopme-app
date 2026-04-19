package de.shopme.data.mapper

import de.shopme.domain.model.SyncStatus as DomainStatus
import de.shopme.data.sync.SyncStatus as EntityStatus

fun EntityStatus.toDomain(progress: Float?): DomainStatus {
    return when (this) {
        EntityStatus.PENDING -> DomainStatus.Pending
        EntityStatus.SYNCING -> DomainStatus.Syncing(progress)
        EntityStatus.FAILED -> DomainStatus.Failed()
        EntityStatus.SYNCED -> DomainStatus.Synced
    }
}