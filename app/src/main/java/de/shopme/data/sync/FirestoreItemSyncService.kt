package de.shopme.data.sync

import com.google.firebase.firestore.FirebaseFirestore
import de.shopme.data.datasource.room.ItemDao
import de.shopme.domain.model.ShoppingItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirestoreItemSyncService(
    private val firestore: FirebaseFirestore,
    private val itemDao: ItemDao
) {

    fun start(listId: String) {

        firestore.collection("lists")
            .document(listId)
            .collection("items")
            .addSnapshotListener { snapshot, _ ->

                val remoteItems = snapshot?.documents?.mapNotNull { doc ->

                    val createdAt =
                        (doc.get("createdAt") as? com.google.firebase.Timestamp)
                            ?.toDate()?.time ?: 0L

                    val updatedAt =
                        (doc.get("updatedAt") as? com.google.firebase.Timestamp)
                            ?.toDate()?.time ?: 0L

                    val deletedAt =
                        (doc.get("deletedAt") as? com.google.firebase.Timestamp)
                            ?.toDate()?.time

                    ShoppingItemEntity(
                        id = doc.id,
                        listId = listId,
                        name = doc.getString("name") ?: return@mapNotNull null,
                        quantity = (doc.getLong("quantity") ?: 1).toInt(),
                        category = doc.getString("category") ?: "Sonstiges",
                        isChecked = doc.getBoolean("isChecked") ?: false,
                        deletedAt = deletedAt,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )

                } ?: emptyList()

                CoroutineScope(Dispatchers.IO).launch {

                    for (remote in remoteItems) {

                        val local = itemDao.getById(remote.id)

                        // 🔥 DELETE HANDLING
                        if (remote.deletedAt != null) {

                            if (local != null) {
                                itemDao.deleteItem(local)
                            }

                            continue
                        }

                        // 🔥 MERGE STRATEGIE (updatedAt)
                        if (local == null || remote.updatedAt > local.updatedAt) {
                            itemDao.upsert(remote)
                        }
                    }
                }
            }
    }
}