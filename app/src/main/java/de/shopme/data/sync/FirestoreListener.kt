package de.shopme.data.sync

import de.shopme.data.datasource.firestore.FirestoreDataSource
import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import de.shopme.domain.model.ShoppingItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FirestoreListener(
    private val dataSource: FirestoreDataSource,
    private val itemDao: ItemDao,
    private val listDao: ListDao,
    private val conflictResolver: ConflictResolver
) {

    fun startListSync(userId: String) {

        CoroutineScope(Dispatchers.IO).launch {

            dataSource.observeListsForUser(userId)
                .collectLatest { remoteLists ->

                    listDao.insertLists(remoteLists)
                }
        }
    }

    fun startItemSync(listId: String) {

        CoroutineScope(Dispatchers.IO).launch {

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
    }
}