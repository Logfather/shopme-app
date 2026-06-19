package de.shopme.data.sync.runtime

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ListenerActivationRegistry {

    private val mutex = Mutex()

    private val requestedLists =
        linkedSetOf<String>()

    private val activeLists =
        linkedSetOf<String>()

    suspend fun requestActivation(
        listId: String
    ) {
        mutex.withLock {
            requestedLists.add(listId)
        }
    }

    suspend fun markActive(
        listId: String
    ) {
        mutex.withLock {
            activeLists.add(listId)
        }
    }

    suspend fun markInactive(
        listId: String
    ) {
        mutex.withLock {
            activeLists.remove(listId)
            requestedLists.remove(listId)
        }
    }

    suspend fun getRequestedLists(): List<String> {
        return mutex.withLock {
            requestedLists.toList()
        }
    }

    suspend fun getActiveLists(): List<String> {
        return mutex.withLock {
            activeLists.toList()
        }
    }

    suspend fun isAlreadyActive(
        listId: String
    ): Boolean {
        return mutex.withLock {
            activeLists.contains(listId)
        }
    }

    suspend fun clear() {
        mutex.withLock {
            requestedLists.clear()
            activeLists.clear()
        }
    }
}