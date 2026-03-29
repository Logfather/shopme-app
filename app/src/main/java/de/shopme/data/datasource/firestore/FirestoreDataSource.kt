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

                    val lists = snapshot?.documents?.mapNotNull { it.toShoppingListEntity() }
                        ?: emptyList()

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

                    val lists = snapshot?.documents?.mapNotNull { it.toShoppingListEntity() }
                        ?: emptyList()

                    trySend(lists)
                }

            awaitClose { listener.remove() }
        }

        return combine(ownedFlow, sharedFlow) { owned, shared ->
            (owned + shared).distinctBy { it.id }
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
                    version = (doc.getLong("version") ?: 0).toInt(),
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

        try {

            

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

        val docRef = itemsRef(listId).document(item.id)

        firestore.runTransaction { transaction ->

            val snapshot = transaction.get(docRef)
            val serverVersion = (snapshot.getLong("version") ?: 0).toInt()

            if (serverVersion != item.version) {
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
                    "sharedWith" to emptyList<String>() // 🔥 wichtig für später
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

        //val itemCount = (getLong("itemCount") ?: 0).toInt()
        val itemCount = (getLong("itemCount") ?: 0).toInt()

        return ShoppingListEntity(
            id = id,
            name = name,
            ownerId = ownerId,
            sharedWith = sharedWith,
            storeTypes = storeTypes,
            itemCount = itemCount,
            createdAt = createdAt,
            updatedAt = updatedAt
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

    suspend fun deleteList(listId: String) {
        firestore
            .collection("lists")
            .document(listId)
            .delete()
            .await()
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