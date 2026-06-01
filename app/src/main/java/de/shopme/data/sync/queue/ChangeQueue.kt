package de.shopme.data.sync.queue

import de.shopme.data.sync.logging.RuntimeLog
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ChangeQueue {

    private val mutex =
        Mutex()

    suspend fun <T> enqueue(
        tag: String,
        block: suspend () -> T
    ): T {

        return mutex.withLock {

            RuntimeLog.queue(
                "Executing: $tag"
            )

            try {

                block()

            } catch (e: Exception) {

                RuntimeLog.queue(
                    "Failed: $tag",
                    e
                )

                throw e
            }
        }
    }
}