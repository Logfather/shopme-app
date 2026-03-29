package de.shopme.presentation.mapper

import android.util.Log
import de.shopme.domain.model.SyncStatus
import de.shopme.presentation.model.SyncUiState

fun SyncStatus.toUiState(): SyncUiState {
    return when (this) {

        is SyncStatus.Pending -> SyncUiState(
            status = this
        )

        is SyncStatus.Syncing -> SyncUiState(
            status = this,
            progress = this.progress
        )

        is SyncStatus.Synced -> SyncUiState(
            status = this
        )

        is SyncStatus.Failed -> SyncUiState(
            status = this,
            canRetry = true

        )
    }
}