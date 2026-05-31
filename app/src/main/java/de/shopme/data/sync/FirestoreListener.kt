package de.shopme.data.sync

import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import de.shopme.data.sync.logging.RecoveryLog
import de.shopme.data.sync.logging.SyncLog
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

        SyncLog.realtime(
            "[Lists] Start sync user=$userId"
        )

        appScope.launch {

            dataSource.observeListsForUser(userId)
                .collectLatest { remoteLists ->

                    SyncLog.realtime(
                        "[Lists] Received count=${remoteLists.size}"
                    )

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
            SyncLog.guard(
                "[Realtime] Already running list=$listId"
            )
            return
        }

        SyncLog.realtime(
            "[Items] Start sync list=$listId"
        )

        val job = appScope.launch {

            dataSource.observeItems(listId)
                .collectLatest { remoteItems ->

                    SyncLog.realtime(
                        "[Items] Received count=${remoteItems.size} list=$listId"
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

                                SyncLog.apply(
                                    "[Insert] Remote item=${remote.id}"
                                )

                                itemDao.upsert(remote)

                                return@forEach
                            }

                            // ------------------------------------------------
                            // CASE 2
                            // Remote event is stale compared to local Room
                            // ------------------------------------------------

                            if (remote.updatedAt < local.updatedAt) {

                                SyncLog.conflict(
                                    "[Stale] Ignore item=${remote.id} " +
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

                                SyncLog.conflict(
                                    "[Reordered] Ignore item=${remote.id} " +
                                            "remote=${remote.updatedAt} " +
                                            "latestApplied=$latestApplied"
                                )

                                return@forEach
                            }

                            // ------------------------------------------------
                            // CASE 4
                            // Remote newer or equal
                            // ------------------------------------------------

                            SyncLog.apply(
                                "[Update] Remote item=${remote.id}"
                            )

                            itemDao.upsert(remote)

                            // ------------------------------------------------
                            // TRACK LAST APPLIED REMOTE VERSION
                            // ------------------------------------------------

                            latestAppliedRemoteVersion[remote.id] =
                                remote.updatedAt

                        } catch (e: Exception) {

                            RecoveryLog.processError(
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

        SyncLog.lifecycle(
            "[Realtime] Stop all listeners"
        )

        try {
            registrationMap.values.forEach { registration ->
                registration.remove()
            }
            registrationMap.clear()
        } catch (e: Exception) {

            RecoveryLog.processError(
                "Failed to remove listeners",
                e
            )
        }
    }
}