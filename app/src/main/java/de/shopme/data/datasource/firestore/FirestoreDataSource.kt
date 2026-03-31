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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.Timestamp
import java.util.UUID

class FirestoreDataSource {

    private val firestore = FirebaseFirestore.getInstance()

    private fun requireUid(): String =
        FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")

    // ============================================================
    // LISTS
    // ============================================================

    fun observeMemberships(userId: String): Flow<List<String>> = callbackFlow {

        val listener = firestore
            .collection("list_members")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("Firestore", "Membership listener error", error)
                    return@addSnapshotListener
                }

                val listIds = snapshot?.documents
                    ?.mapNotNull { it.getString("listId") }
                    ?.distinct()
                    ?: emptyList()

                trySend(listIds)
            }

        awaitClose { listener.remove() }
    }

    fun observeListById(listId: String): Flow<ShoppingListEntity?> = callbackFlow {

        val listener = firestore
            .collection("lists")
            .document(listId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("Firestore", "List listener error", error)
                    return@addSnapshotListener
                }

                val list = snapshot?.toShoppingListEntity()

                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    fun observeListsForUser(uid: String): Flow<List<ShoppingListEntity>> {

        val ownedFlow = callbackFlow {

            val listener = firestore
                .collection("lists")
                .whereEqualTo("ownerId", uid)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        Log.e("Firestore", "Owned listener error", error)
                        return@addSnapshotListener
                    }

                    val lists = snapshot?.documents
                        ?.mapNotNull { it.toShoppingListEntity() }
                        ?: emptyList()

                    Log.d("LIST_DEBUG", "FILTERED owned lists: ${lists.map { it.ownerId }}")

                    trySend(lists)
                }

            awaitClose { listener.remove() }
        }

        val sharedFlow = callbackFlow {

            val listener = firestore
                .collection("lists")
                .whereArrayContains("sharedWith", uid)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        Log.e("Firestore", "Shared listener error", error)
                        return@addSnapshotListener
                    }

                    val lists = snapshot?.documents
                        ?.mapNotNull { it.toShoppingListEntity() }
                        ?.filter { uid in it.sharedWith }   // 🔥 WICHTIG
                        ?: emptyList()

                    trySend(lists)
                }

            awaitClose { listener.remove() }
        }

        return combine(ownedFlow, sharedFlow) { owned, shared ->
            (owned + shared).distinctBy { it.id }
        }
    }

    suspend fun softDeleteList(listId: String) {

        Log.d("DELETE", "Firestore HARD DELETE CALLED for $listId")

        try {
            firestore
                .collection("lists")
                .document(listId)
                .delete()
                .await()

            Log.d("DELETE", "Firestore HARD DELETE SUCCESS for $listId")

        } catch (e: Exception) {
            Log.e("DELETE", "hardDeleteList FAILED", e)
        }
    }

    // ============================================================
    // ITEMS
    // ============================================================

    private fun itemsRef(listId: String) =
        firestore.collection("lists")
            .document(listId)
            .collection("items")   // 👈 GENAU HIER

    suspend fun getItemsOnce(listId: String): List<ShoppingItemEntity> {

        return try {

            val snapshot = itemsRef(listId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->

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
                    listId = listId,
                    name = doc.getString("name") ?: return@mapNotNull null,
                    quantity = (doc.getLong("quantity") ?: 1).toInt(),
                    category = doc.getString("category") ?: "Sonstiges",
                    isChecked = doc.getBoolean("isChecked") ?: false,
                    deletedAt = deletedAt,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
            }

        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getListOnce(listId: String): ShoppingListEntity? {
        return try {

            val doc = firestore
                .collection("lists")
                .document(listId)
                .get()
                .await()

            if (!doc.exists()) {
                return null
            }

            return doc.toShoppingListEntity()


        } catch (e: Exception) {
            null
        }
    }
    suspend fun getItemVersion(
        listId: String,
        itemId: String
    ): Int? {
        return try {
            val snapshot = itemsRef(listId)
                .document(itemId)
                .get()
                .await()

            (snapshot.getLong("version") ?: 0L).toInt()

        } catch (e: Exception) {
            null
        }
    }

    fun observeItems(listId: String): Flow<List<ShoppingItemEntity>> =
        callbackFlow {

            val listener = itemsRef(listId)
                .whereEqualTo("deletedAt", null)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
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

                    trySend(items)
                }

            awaitClose { listener.remove() }
        }

    // ============================================================
    // WRITE OPERATIONS
    // ============================================================

    suspend fun addItem(listId: String, item: ShoppingItemEntity) {

        try {

            itemsRef(listId)
                .document(item.id)
                .set(
                    mapOf(
                        "name" to item.name,
                        "quantity" to item.quantity,
                        "category" to item.category,
                        "isChecked" to item.isChecked,
                        "deletedAt" to item.deletedAt,
                        "createdAt" to item.createdAt,
                        "updatedAt" to item.updatedAt
                        // 🔥 ownerId erstmal RAUS!
                    )
                )
                .await()

            

        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateItem(listId: String, item: ShoppingItemEntity) {

        itemsRef(listId)
            .document(item.id)
            .update(
                mapOf(
                    "name" to item.name,
                    "quantity" to item.quantity,
                    "category" to item.category,
                    "isChecked" to item.isChecked,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .await()
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

    suspend fun createList(list: ShoppingListEntity) {

        firestore
            .collection("lists")
            .document(list.id)
            .set(
                mapOf(
                    "name" to list.name,
                    "ownerId" to list.ownerId,
                    "storeTypes" to list.storeTypes.map { it.name },
                    "itemCount" to list.itemCount,
                    "createdAt" to list.createdAt,
                    "updatedAt" to list.updatedAt,
                    "sharedWith" to emptyList<String>(),
                    "deletedAt" to null
                )
            )
            .await()
    }

    suspend fun addUserToList(
        listId: String,
        userId: String
    ) {
        firestore
            .collection("lists")
            .document(listId)
            .update(
                "sharedWith",
                com.google.firebase.firestore.FieldValue.arrayUnion(userId)
            )
            .await()
    }

    fun DocumentSnapshot.toShoppingListEntity(): ShoppingListEntity? {

        val name = getString("name") ?: return null

        val ownerId = getString("ownerId") ?: ""

        val storeTypes =
            (get("storeTypes") as? List<String>)
                ?.mapNotNull {
                    runCatching { StoreType.valueOf(it) }.getOrNull()
                } ?: emptyList()

        val sharedWith =
            (get("sharedWith") as? List<*>)
                ?.filterIsInstance<String>()
                ?: emptyList()

        val createdAt =
            (get("createdAt") as? Timestamp)
                ?.toDate()?.time ?: System.currentTimeMillis()

        val updatedAt =
            (get("updatedAt") as? Timestamp)
                ?.toDate()?.time ?: System.currentTimeMillis()

        val deletedAt =
            (get("deletedAt") as? Timestamp)
                ?.toDate()?.time

        val itemCount = (getLong("itemCount") ?: 0).toInt()

        return ShoppingListEntity(
            id = id,
            name = name,
            ownerId = ownerId,
            sharedWith = sharedWith,
            storeTypes = storeTypes,
            itemCount = itemCount,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt
        )
    }

    suspend fun debugDumpAllLists() {
        try {
            val snapshot = firestore
                .collection("lists")
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                
            }

        } catch (e: Exception) {
        }
    }

    // ============================================================
    // LISTS INVITE
    // ============================================================

    suspend fun createInvite(): String {

        val inviteId = UUID.randomUUID().toString()

        firestore
            .collection("invites")
            .document(inviteId)
            .set(
                mapOf(
                    "createdBy" to requireUid(),
                    "createdAt" to System.currentTimeMillis(),
                    "status" to "active"
                )
            )
            .await()

        return inviteId
    }

    suspend fun getInvite(inviteId: String): String? {

        val doc = firestore
            .collection("invites")
            .document(inviteId)
            .get()
            .await()

        return doc.getString("createdBy")
    }

    suspend fun getListsForUser(userId: String): List<ShoppingListEntity> {

        val snapshot = firestore
            .collection("lists")
            .whereEqualTo("ownerId", userId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.toShoppingListEntity() }
    }

}