package de.shopme.data.mapper

import de.shopme.domain.model.SyncStatus as DomainStatus
import de.shopme.data.sync.SyncStatus as EntityStatus

fun EntityStatus.toDomain(
    progress: Float?,
    retryCount: Int = 0
): DomainStatus {
    return when (this) {
        EntityStatus.PENDING -> DomainStatus.Pending

        EntityStatus.SYNCING -> DomainStatus.Syncing(
            progress = progress ?: 0f
        )

        EntityStatus.FAILED -> DomainStatus.Failed(
            retryCount = retryCount
        )

        EntityStatus.SYNCED -> DomainStatus.Synced
    }
}