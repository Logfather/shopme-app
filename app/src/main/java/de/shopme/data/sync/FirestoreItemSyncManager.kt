package de.shopme.data.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import de.shopme.data.datasource.room.ItemDao
import de.shopme.domain.model.ShoppingItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirestoreItemSyncManager(
    private val firestore: FirebaseFirestore,
    private val itemDao: ItemDao
) {

    private val listeners = mutableMapOf<String, ListenerRegistration>()

    fun startListSync(listId: String) {

        // 🔒 doppelte Listener vermeiden
        if (listeners.containsKey(listId)) return

        val listener = firestore.collection("lists")
            .document(listId)
            .collection("items")
            .addSnapshotListener { snapshot, error ->

                if (error != null) return@addSnapshotListener

                val items = snapshot?.documents?.mapNotNull { doc ->

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

                    for (remote in items) {

                        val local = itemDao.getById(remote.id)

                        if (remote.deletedAt != null) {

                            if (local != null) {
                                itemDao.deleteItem(local) // ✅ dein DAO braucht Entity
                            }

                            continue
                        }

                        if (local == null || remote.updatedAt > local.updatedAt) {
                            itemDao.upsert(remote)
                        }
                    }
                }
            }

        listeners[listId] = listener
    }

    fun stopListSync(listId: String) {
        listeners.remove(listId)?.remove()
    }

    fun stopAll() {
        listeners.values.forEach { it.remove() }
        listeners.clear()
    }
}