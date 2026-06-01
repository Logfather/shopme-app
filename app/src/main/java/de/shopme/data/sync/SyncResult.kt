package de.shopme.data.sync

sealed interface SyncResult {

    data object Success : SyncResult

    data object Retry : SyncResult

    data object NoOp : SyncResult

    data class Failure(
        val throwable: Throwable
    ) : SyncResult
}