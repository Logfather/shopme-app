package de.shopme.presentation.model

import de.shopme.domain.model.SyncStatus

data class SyncUiState(
    val status: SyncStatus,
    val progress: Float? = null,
    val canRetry: Boolean = false
)