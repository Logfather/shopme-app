package de.shopme.data.sync

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ChangeQueue {

    private val mutex = Mutex()

    suspend fun <T> enqueue(tag: String, block: suspend () -> T): T {

        return mutex.withLock {

            Log.d("CHANGE_QUEUE", "Executing: $tag")

            try {
                block()
            } catch (e: Exception) {
                Log.e("CHANGE_QUEUE", "Failed: $tag", e)
                throw e
            }
        }
    }
}