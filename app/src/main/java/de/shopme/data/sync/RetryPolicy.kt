package de.shopme.data.sync

object RetryPolicy {

    private const val BASE_DELAY_MS = 1_000L
    private const val MAX_DELAY_MS = 60_000L

    fun calculateDelay(retryCount: Int): Long {
        val delay = BASE_DELAY_MS * (1 shl retryCount)
        return delay.coerceAtMost(MAX_DELAY_MS)
    }

    fun shouldRetry(
        retryCount: Int,
        lastAttemptAt: Long?
    ): Boolean {

        if (lastAttemptAt == null) return true

        val delay = calculateDelay(retryCount)
        val now = System.currentTimeMillis()

        return now - lastAttemptAt >= delay
    }
}