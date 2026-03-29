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

    fun startListSync(userId: String) {

        Log.d("LIST_DEBUG", "startListSync CALLED for user=$userId")

        appScope.scope.launch {

            fun startListSync(userId: String) {

                Log.d("LIST_DEBUG", "startListSync CALLED for user=$userId")

                // 🔥 FIX: eigener Scope statt appScope
                kotlinx.coroutines.GlobalScope.launch {

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

                                listDao.upsert(list)

                                startItemSync(list.id)
                            }
                        }
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

                        if (conflictResolver.shouldApplyRemote(local, remote)) {
                            itemDao.upsert(remote)
                        }
                    }
                }
        }

        activeItemListeners[listId] = job
    }
}