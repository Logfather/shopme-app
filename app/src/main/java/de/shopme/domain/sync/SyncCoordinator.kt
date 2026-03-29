package de.shopme.domain.sync

import de.shopme.domain.model.SyncStatus
import de.shopme.presentation.model.SyncUiState

fun mapToUiState(syncState: SyncStatus): SyncUiState {
    return when (syncState) {

        is SyncStatus.Pending -> SyncUiState(
            status = syncState
        )

        is SyncStatus.Syncing -> SyncUiState(
            status = syncState,
            progress = syncState.progress
        )

        is SyncStatus.Synced -> SyncUiState(
            status = syncState
        )

        is SyncStatus.Failed -> SyncUiState(
            status = syncState,
            canRetry = syncState.canRetry
        )
    }
}