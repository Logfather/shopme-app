package de.shopme.domain.model

data class SyncOverview(
    val pending: Int = 0,
    val syncing: Int = 0,
    val failed: Int = 0
) {
    val hasErrors: Boolean
        get() = failed > 0

    val isBusy: Boolean
        get() = pending > 0 || syncing > 0
}