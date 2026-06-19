package de.shopme.data.sync

import com.google.firebase.firestore.ListenerRegistration
import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import de.shopme.data.sync.logging.RecoveryLog
import de.shopme.data.sync.logging.SyncLog
import de.shopme.data.sync.runtime.SyncBootstrapper
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


    lateinit var bootstrapper: SyncBootstrapper

    private var initialLoadCompleted = false

    private val activeMembershipJob =
        mutableListOf<Job>()

    private val activeItemListeners =
        mutableMapOf<String, Job>()

    private val registrationMap =
        mutableMapOf<String, ListenerRegistration>()

    // ------------------------------------------------------------
    // MEMBERSHIP / LIST DISCOVERY
    // ------------------------------------------------------------

    fun startListSync(
        userId: String
    ) {

        SyncLog.realtime(
            "[Lists] Start membership sync user=$userId"
        )

        val job = appScope.launch {

            dataSource.observeListsForUser(userId)
                .collectLatest { remoteLists ->

                    SyncLog.realtime(
                        "[Lists] Received count=${remoteLists.size}"
                    )

                    val remoteIds =
                        remoteLists.map { it.id }

                    // --------------------------------------------------------
                    // DELETE SYNC
                    // ONLY AFTER INITIAL LOAD
                    // --------------------------------------------------------

                    if (initialLoadCompleted) {

                        if (remoteIds.isEmpty()) {

                            listDao.clearAll()

                        } else {

                            listDao.deleteAllExcept(
                                remoteIds
                            )
                        }
                    }

                    // --------------------------------------------------------
                    // UPSERT LISTS
                    // --------------------------------------------------------

                    remoteLists.forEach { list ->

                        listDao.upsert(list)

                        // ----------------------------------------------------
                        // IMPORTANT:
                        // NO DIRECT REALTIME ATTACH HERE
                        // ----------------------------------------------------

                        bootstrapper
                            .activateList(list.id)
                    }

                    // --------------------------------------------------------
                    // INITIAL LOAD COMPLETED
                    // --------------------------------------------------------

                    if (
                        !initialLoadCompleted &&
                        remoteLists.isNotEmpty()
                    ) {

                        initialLoadCompleted = true

                        SyncLog.realtime(
                            "[Lists] Initial load completed"
                        )
                    }
                }
        }

        activeMembershipJob.add(job)
    }

    // ------------------------------------------------------------
    // REALTIME ITEM LISTENER
    // ------------------------------------------------------------

    fun startItemSync(
        listId: String
    ) {

        if (
            activeItemListeners.containsKey(listId)
        ) {

            SyncLog.guard(
                "[Realtime] Already running list=$listId"
            )

            return
        }

        SyncLog.realtime(
            "[Items] Start realtime list=$listId"
        )

        val job = appScope.launch {

            dataSource.observeItems(listId)
                .collectLatest { remoteItems ->

                    SyncLog.realtime(
                        "[Items] Received count=${remoteItems.size} list=$listId"
                    )

                    remoteItems.forEach { remote ->

                        try {

                            // ------------------------------------------------
                            // CENTRALIZED APPLY ENGINE
                            // ------------------------------------------------

                            bootstrapper
                                .syncCoordinator
                                .applyRealtimeItem(remote)

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

    fun stopItemSync(
        listId: String
    ) {

        SyncLog.realtime(
            "[Items] Stop realtime list=$listId"
        )

        activeItemListeners[listId]
            ?.cancel()

        activeItemListeners
            .remove(listId)
    }

    // ------------------------------------------------------------
    // STOP ALL
    // ------------------------------------------------------------

    fun stop() {

        SyncLog.lifecycle(
            "[Realtime] Stop all listeners"
        )

        try {

            activeMembershipJob.forEach {
                it.cancel()
            }

            activeMembershipJob.clear()

            activeItemListeners.values.forEach {
                it.cancel()
            }

            activeItemListeners.clear()

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