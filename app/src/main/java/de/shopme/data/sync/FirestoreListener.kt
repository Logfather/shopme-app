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

        Log.d("LIST_DEBUG", "startListSync DISABLED (replaced by MembershipListener) for user=$userId")

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

        if (activeItemListeners.containsKey(listId)) {
            Log.d("ITEM_SYNC", "Already running for list=$listId → skip")
            return
        }

        Log.d("ITEM_SYNC", "Start sync for list=$listId")

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