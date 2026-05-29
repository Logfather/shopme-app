package de.shopme.data.sync

import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FirestoreListener(
    private val dataSource: FirestoreGateway,
    private val itemDao: ItemDao,
    private val listDao: ListDao,
    private val conflictResolver: ConflictResolver,
    private val appScope: CoroutineScope
) {
    private var initialLoadCompleted = false   // 🔥 HIERHIN

    private val activeItemListeners = mutableMapOf<String, Job>()

    private val activeItemSyncs = mutableSetOf<String>()

    private val activeListSyncs = mutableSetOf<String>()

    private val registrationMap = mutableMapOf<String, ListenerRegistration>()

    private val latestAppliedRemoteVersion =
        mutableMapOf<String, Long>()

    fun startListSync(userId: String) {

        //Log.d("LIST_DEBUG", "startListSync ACTIVE (observeListsForUser) for user=$userId")

        appScope.launch {

            dataSource.observeListsForUser(userId)
                .collectLatest { remoteLists ->

                    //Log.d("LIST_SYNC", "Received ${remoteLists.size} lists from Firestore")

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
            //Log.d("ITEM_SYNC", "Already running for list=$listId → skip")
            return
        }

        //Log.d("ITEM_SYNC", "Start sync for list=$listId")

        val job = appScope.launch {

            dataSource.observeItems(listId)
                .collectLatest { remoteItems ->

                    Log.d(
                        "ITEM_SYNC",
                        "Received ${remoteItems.size} remote items for list=$listId"
                    )

                    remoteItems.forEach { remote ->

                        try {

                            val local =
                                itemDao.getById(remote.id)

                            // ------------------------------------------------
                            // CASE 1
                            // Local item missing
                            // ------------------------------------------------

                            if (local == null) {

                                Log.d(
                                    "ITEM_SYNC",
                                    "APPLY REMOTE INSERT id=${remote.id}"
                                )

                                itemDao.upsert(remote)

                                return@forEach
                            }

                            // ------------------------------------------------
                            // CASE 2
                            // Remote event is stale compared to local Room
                            // ------------------------------------------------

                            if (remote.updatedAt < local.updatedAt) {

                                Log.w(
                                    "ITEM_SYNC",
                                    "IGNORE STALE REMOTE id=${remote.id} " +
                                            "remote=${remote.updatedAt} " +
                                            "local=${local.updatedAt}"
                                )

                                return@forEach
                            }

                            // ------------------------------------------------
                            // CASE 3
                            // Remote event already superseded historically
                            // ------------------------------------------------

                            val latestApplied =
                                latestAppliedRemoteVersion[remote.id]

                            if (
                                latestApplied != null &&
                                remote.updatedAt < latestApplied
                            ) {

                                Log.w(
                                    "ITEM_SYNC",
                                    "IGNORE REORDERED REMOTE id=${remote.id} " +
                                            "remote=${remote.updatedAt} " +
                                            "latestApplied=$latestApplied"
                                )

                                return@forEach
                            }

                            // ------------------------------------------------
                            // CASE 4
                            // Remote newer or equal
                            // ------------------------------------------------

                            Log.d(
                                "ITEM_SYNC",
                                "APPLY REMOTE UPDATE id=${remote.id}"
                            )

                            itemDao.upsert(remote)

                            // ------------------------------------------------
                            // TRACK LAST APPLIED REMOTE VERSION
                            // ------------------------------------------------

                            latestAppliedRemoteVersion[remote.id] =
                                remote.updatedAt

                        } catch (e: Exception) {

                            Log.e(
                                "ITEM_SYNC",
                                "Realtime apply failed id=${remote.id}",
                                e
                            )
                        }
                    }
                }
        }

        activeItemListeners[listId] = job
    }

    fun stop() {
        //Log.d("FS_LISTENER", "Stopping all listeners")

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