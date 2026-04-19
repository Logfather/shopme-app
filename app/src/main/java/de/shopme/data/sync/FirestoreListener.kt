package de.shopme.data.sync

import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import de.shopme.core.AppScope
import de.shopme.data.datasource.firestore.FirestoreDataSource
import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FirestoreListener(
    private val dataSource: FirestoreDataSource,
    private val itemDao: ItemDao,
    private val listDao: ListDao,
    private val conflictResolver: ConflictResolver,
    private val appScope: AppScope
) {
    private var initialLoadCompleted = false   // 🔥 HIERHIN

    private val activeItemListeners = mutableMapOf<String, Job>()

    private val activeItemSyncs = mutableSetOf<String>()

    private val activeListSyncs = mutableSetOf<String>()

    private val registrationMap = mutableMapOf<String, ListenerRegistration>()

    fun startListSync(userId: String) {

        Log.d("LIST_DEBUG", "startListSync ACTIVE (observeListsForUser) for user=$userId")

        appScope.scope.launch {

            dataSource.observeListsForUser(userId)
                .collectLatest { remoteLists ->

                    Log.d("LIST_SYNC", "Received ${remoteLists.size} lists from Firestore")

                    val remoteIds = remoteLists.map { it.id }

                    if (initialLoadCompleted) {

                        // 🔥 DELETE SYNC NUR NACH INITIAL LOAD
                        if (remoteIds.isEmpty()) {
                            listDao.clearAll()
                        } else {
                            listDao.deleteAllExcept(remoteIds)
                        }
                    }

                    // 🔥 UPSERT IMMER
                    remoteLists.forEach { list ->

                        listDao.upsert(list)

                        if (!activeItemSyncs.contains(list.id)) {
                            activeItemSyncs.add(list.id)
                            startItemSync(list.id)
                        }
                    }

                    // 🔥 MARK INITIAL LOAD DONE
                    if (!initialLoadCompleted && remoteLists.isNotEmpty()) {
                        initialLoadCompleted = true
                    }
                }
        }
    }

    fun startItemSync(listId: String) {

        if (activeItemListeners.containsKey(listId)) {
            Log.d("ITEM_SYNC", "Already running for list=$listId → skip")
            return
        }

        Log.d("ITEM_SYNC", "Start sync for list=$listId")

        val job = appScope.scope.launch {

            dataSource.observeItems(listId)
                .collectLatest { remoteItems ->

                    // 🔥 Kein Write mehr hier – SyncCoordinator ist die einzige Write-Quelle

                    Log.d(
                        "ITEM_SYNC",
                        "Received ${remoteItems.size} remote items for list=$listId"
                    )
                }
        }

        activeItemListeners[listId] = job
    }

    fun stop() {
        Log.d("FS_LISTENER", "Stopping all listeners")

        try {
            registrationMap.values.forEach { registration ->
                registration.remove()
            }
            registrationMap.clear()
        } catch (e: Exception) {
            Log.e("FS_LISTENER", "Failed to remove listeners", e)
        }
    }
}