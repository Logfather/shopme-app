package de.shopme.data.sync.runtime

import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.ConcurrentHashMap

class EntityMutexRegistry {

    private val mutexes =
        ConcurrentHashMap<String, Mutex>()

    fun mutexFor(
        entityId: String
    ): Mutex {

        return mutexes.getOrPut(entityId) {
            Mutex()
        }
    }
}