package de.shopme.data.sync

object RetryPolicy {

    private const val BASE_DELAY = 1_000L       // 1s
    private const val MAX_DELAY = 60_000L       // 60s
    private const val MAX_RETRIES = 5

    fun shouldRetry(
        retryCount: Int,
        lastAttemptAt: Long?
    ): Boolean {

        if (retryCount >= MAX_RETRIES) return false

        if (lastAttemptAt == null) return true

        val delay = computeDelay(retryCount)

        val nextAllowed = lastAttemptAt + delay

        return System.currentTimeMillis() >= nextAllowed
    }

    fun computeDelay(retryCount: Int): Long {

        val exp = BASE_DELAY * (1 shl retryCount)

        val jitter = (0..500).random()

        return minOf(exp + jitter, MAX_DELAY)
    }
}