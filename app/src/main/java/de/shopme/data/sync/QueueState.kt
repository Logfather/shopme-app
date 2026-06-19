package de.shopme.data.sync

enum class QueueState {
    PENDING,
    PROCESSING,
    RETRY_WAIT,
    DONE,
    FAILED
}