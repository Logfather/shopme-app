package de.shopme.data.sync

import android.util.Log
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

    private val activeItemListeners = mutableMapOf<String, Job>()

    private val activeItemSyncs = mutableSetOf<String>()

    private val activeListSyncs = mutableSetOf<String>()

    fun startListSync(userId: String) {

        Log.d("LIST_DEBUG", "startListSync CALLED for user=$userId")

        appScope.scope.launch {

            dataSource.observeListsForUser(userId)
                .collectLatest { remoteLists ->

                    Log.d("LIST_DEBUG", "LIST LISTENER TRIGGERED size=${remoteLists.size}")

                    remoteLists.forEach {
                        Log.d(
                            "LIST_DEBUG",
                            "REMOTE list=${it.name} id=${it.id} owner=${it.ownerId}"
                        )
                    }

                    val uniqueLists = remoteLists.distinctBy { it.id }

                    uniqueLists.forEach { list ->

                        val local = listDao.getListOnce(list.id)

                        // 🔥 FIX: Undo erlauben, Zombie-Reinsert verhindern
                        if (local?.deletedAt != null && list.deletedAt != null) {
                            Log.d("SYNC", "IGNORE remote delete for already deleted ${list.id}")
                            return@forEach
                        }

                        // 🔥 FIX: Remote ist Quelle der Wahrheit
                        if (list.deletedAt != null) {
                            Log.d("SYNC", "APPLY remote delete for ${list.id}")
                            listDao.markDeleted(list.id, list.deletedAt)
                        } else {
                            Log.d("SYNC", "UPSERT remote list ${list.id}")
                            listDao.upsert(list)
                        }

                        if (!activeItemSyncs.contains(list.id)) {
                            activeItemSyncs.add(list.id)
                            startItemSync(list.id)
                        }
                    }
                }
        }
    }

    private fun observeMemberships(userId: String) {

        appScope.scope.launch {

            dataSource.observeMemberships(userId)
                .collectLatest { listIds ->

                    Log.d("LIST_DEBUG", "Membership listIds=$listIds")

                    listIds.forEach { listId ->

                        if (!activeListSyncs.contains(listId)) {
                            activeListSyncs.add(listId)
                            startSingleListSync(listId)
                        }
                    }
                }
        }
    }

    private fun startSingleListSync(listId: String) {

        appScope.scope.launch {

            dataSource.observeListById(listId)
                .collectLatest { list ->

                    if (list == null) return@collectLatest

                    listDao.upsert(list)

                    if (!activeItemSyncs.contains(list.id)) {
                        activeItemSyncs.add(list.id)
                        startItemSync(list.id)
                    }
                }
        }
    }

    fun startItemSync(listId: String) {

        activeItemListeners[listId]?.cancel()

        val job = appScope.scope.launch {

            dataSource.observeItems(listId)
                .collectLatest { remoteItems ->

                    remoteItems.forEach { remote ->

                        val local = itemDao.getById(remote.id)

                        // 🔥 verhindert Zombie-Reinsert
                        if (local?.deletedAt != null) return@forEach

                        if (conflictResolver.shouldApplyRemote(local, remote)) {
                            itemDao.upsert(remote)
                        }
                    }
                }
        }

        activeItemListeners[listId] = job
    }
}