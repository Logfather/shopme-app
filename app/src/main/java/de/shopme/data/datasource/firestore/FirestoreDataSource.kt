package de.shopme.data.datasource.firestore

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreDataSource {

    private val firestore = FirebaseFirestore.getInstance()

    private fun requireUid(): String =
        FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")

    // ============================================================
    // LISTS
    // ============================================================

    fun observeListsForUser(uid: String): Flow<List<ShoppingListEntity>> =
        callbackFlow {

            val listener = firestore
                .collection("lists")
                .whereEqualTo("ownerId", uid)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        Log.e("Firestore", "Listener error", error)
                        return@addSnapshotListener
                    }

                    val lists = snapshot?.documents?.mapNotNull { document ->

                        val storeTypes =
                            (document.get("storeTypes") as? List<String>)
                                ?.mapNotNull {
                                    runCatching { StoreType.valueOf(it) }
                                        .getOrNull()
                                } ?: emptyList()

                        val createdAt =
                            (document.get("createdAt") as? com.google.firebase.Timestamp)
                                ?.toDate()?.time ?: System.currentTimeMillis()

                        val updatedAt =
                            (document.get("updatedAt") as? com.google.firebase.Timestamp)
                                ?.toDate()?.time ?: System.currentTimeMillis()

                        val itemCount = (document.getLong("itemCount") ?: 0).toInt()

                        ShoppingListEntity(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            ownerId = document.getString("ownerId") ?: "",
                            storeTypes = storeTypes,
                            itemCount = itemCount,
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )

                    } ?: emptyList()

                    trySend(lists)
                }

            awaitClose { listener.remove() }
        }

    // ============================================================
    // ITEMS
    // ============================================================

    private fun itemsRef(listId: String) =
        firestore.collection("lists")
            .document(listId)
            .collection("items")

    fun observeItems(listId: String): Flow<List<ShoppingItemEntity>> =
        callbackFlow {

            val uid = requireUid()

            val listener = itemsRef(listId)
                .whereEqualTo("ownerId", uid)
                .whereEqualTo("deletedAt", null)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        Log.e("Firestore", "Items listener failed", error)
                        return@addSnapshotListener
                    }

                    val items = snapshot?.documents?.mapNotNull { doc ->

                        val createdAt =
                            (doc.get("createdAt") as? com.google.firebase.Timestamp)
                                ?.toDate()?.time ?: System.currentTimeMillis()

                        val updatedAt =
                            (doc.get("updatedAt") as? com.google.firebase.Timestamp)
                                ?.toDate()?.time ?: System.currentTimeMillis()

                        val deletedAt =
                            (doc.get("deletedAt") as? com.google.firebase.Timestamp)
                                ?.toDate()?.time

                        ShoppingItemEntity(
                            id = doc.id,
                            listId = listId,   // ✅ DAS IST DER FIX
                            name = doc.getString("name") ?: return@mapNotNull null,
                            quantity = (doc.getLong("quantity") ?: 1).toInt(),
                            category = doc.getString("category") ?: "Sonstiges",
                            isChecked = doc.getBoolean("isChecked") ?: false,
                            version = (doc.getLong("version") ?: 0).toInt(),
                            deletedAt = deletedAt,
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )

                    } ?: emptyList()

                    trySend(items)
                }

            awaitClose { listener.remove() }
        }

    // ============================================================
    // WRITE OPERATIONS
    // ============================================================

    suspend fun addItem(listId: String, item: ShoppingItemEntity) {

        val uid = requireUid()

        itemsRef(listId)
            .document(item.id)
            .set(
                mapOf(
                    "name" to item.name,
                    "quantity" to item.quantity,
                    "category" to item.category,
                    "isChecked" to item.isChecked,
                    "version" to item.version,
                    "deletedAt" to item.deletedAt,
                    "createdAt" to item.createdAt,
                    "updatedAt" to item.updatedAt,
                    "ownerId" to uid
                )
            ).await()
    }

    suspend fun updateItem(listId: String, item: ShoppingItemEntity) {

        val docRef = itemsRef(listId).document(item.id)

        firestore.runTransaction { transaction ->

            val snapshot = transaction.get(docRef)
            val serverVersion = (snapshot.getLong("version") ?: 0).toInt()

            if (serverVersion != item.version) {
                Log.w("FIRESTORE_CONFLICT", "Version conflict for ${item.id}")
                return@runTransaction null
            }

            transaction.update(
                docRef,
                mapOf(
                    "name" to item.name,
                    "quantity" to item.quantity,
                    "category" to item.category,
                    "isChecked" to item.isChecked,
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "version" to serverVersion + 1
                )
            )
        }.await()
    }

    suspend fun deleteItem(listId: String, itemId: String) {

        itemsRef(listId)
            .document(itemId)
            .update(
                mapOf(
                    "deletedAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }
}