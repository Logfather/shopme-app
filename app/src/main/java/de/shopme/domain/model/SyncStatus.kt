package de.shopme.domain.model

sealed class SyncStatus {

    object Synced : SyncStatus()

    object Pending : SyncStatus()

    data class Syncing(
        val progress: Float = 0f
    ) : SyncStatus()

    data class Failed(
        val retryCount: Int
    ) : SyncStatus() {

        val canRetry: Boolean
            get() = retryCount < 5
    }
}