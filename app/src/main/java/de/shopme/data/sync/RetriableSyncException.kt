package de.shopme.data.sync

class RetryableSyncException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)