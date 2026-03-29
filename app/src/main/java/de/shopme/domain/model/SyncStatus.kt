package de.shopme.domain.model

sealed class SyncStatus {

    data object Pending : SyncStatus()

    data class Syncing(
        val progress: Float? = null // optional!
    ) : SyncStatus()

    data object Synced : SyncStatus()

    data class Failed(
        val canRetry: Boolean = true
    ) : SyncStatus()
}