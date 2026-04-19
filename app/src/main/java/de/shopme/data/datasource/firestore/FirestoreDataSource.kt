package de.shopme.data.datasource.firestore

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import de.shopme.domain.model.InviteData
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.util.UUID


class FirestoreDataSource : FirestoreGateway {

    private val firestore = FirebaseFirestore.getInstance()

    private fun requireUid(): String =
        FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")

    // ============================================================
    // LISTS
    // ============================================================

    override suspend fun addMembership(userId: String, listId: String) {

        Log.d("MEMBERSHIP_WRITE", "authUid=${FirebaseAuth.getInstance().currentUser?.uid}")
        Log.d("MEMBERSHIP_WRITE", "targetUserId=$userId listId=$listId")

        firestore
            .collection("lists")
            .document(listId)
            .update(
                "sharedWith",
                FieldValue.arrayUnion(userId)
            )
            .await()
    }

    override suspend fun isUserMemberOfList(
        userId: String,
        listId: String
    ): Boolean {
        return try {

            val doc = firestore
                .collection("lists")
                .document(listId)
                .get()
                .await()

            val sharedWith =
                doc.get("sharedWith") as? List<*>
                    ?: emptyList<Any>()

            val isMember = userId in sharedWith

            Log.d("MEMBERSHIP_DEBUG", "Check membership → list=$listId isMember=$isMember")

            isMember

        } catch (e: Exception) {

            Log.e("MEMBERSHIP_DEBUG", "Check failed", e)

            false
        }
    }

    override suspend fun removeUserFromList(
        listId: String,
        userId: String
    ) {
        firestore
            .collection("lists")
            .document(listId)
            .update(
                "sharedWith",
                FieldValue.arrayRemove(userId)
            )
            .await()
    }

    override fun observeListById(listId: String): Flow<ShoppingListEntity?> = callbackFlow {

        val listener = firestore
            .collection("lists")
            .document(listId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("LIST_FLOW", "Listener error", error)
                    return@addSnapshotListener   // 🔥 FIX
                }

                val list = snapshot?.toShoppingListEntity()

                trySend(list).isSuccess
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
                        Log.e("LIST_FLOW", "Owned listener error", error)
                        return@addSnapshotListener
                    }

                    val lists = snapshot?.documents
                        ?.mapNotNull { it.toShoppingListEntity() }
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
                        Log.e("LIST_FLOW", "Shared listener error", error)
                        return@addSnapshotListener
                    }

                    val lists = snapshot?.documents
                        ?.mapNotNull { it.toShoppingListEntity() }
                        ?.filter { uid in it.sharedWith }
                        ?: emptyList()

                    trySend(lists)
                }

            awaitClose { listener.remove() }
        }

        return combine(ownedFlow, sharedFlow) { owned, shared ->
            (owned + shared).distinctBy { it.id }
        }
    }

    override suspend fun softDeleteList(listId: String) {
        try {
            firestore
                .collection("lists")
                .document(listId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("DELETE", "hardDeleteList FAILED", e)
        }
    }

    fun observeList(listId: String): Flow<ShoppingListEntity?> = callbackFlow {
        val listener = firestore
            .collection("lists")
            .document(listId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("LIST_FLOW", "Listener error", error)
                    return@addSnapshotListener
                }

                val entity = snapshot?.toShoppingListEntity()
                trySend(entity).isSuccess
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

    suspend fun getListOnce(listId: String): ShoppingListEntity? {
        return try {
            val doc = firestore
                .collection("lists")
                .document(listId)
                .get()
                .await()

            if (!doc.exists()) return null

            doc.toShoppingListEntity()

        } catch (e: Exception) {
            Log.e("FIRESTORE", "getListOnce FAILED", e)
            throw e
        }
    }

    override suspend fun getItemVersion(
        listId: String,
        itemId: String
    ): Long? {
        return try {
            val snapshot = itemsRef(listId)
                .document(itemId)
                .get()
                .await()

            snapshot.getLong("updatedAt")

        } catch (e: Exception) {
            null
        }
    }

    override fun observeItems(listId: String): Flow<List<ShoppingItemEntity>> =
        callbackFlow {

            val listener = itemsRef(listId)
                .whereEqualTo("deletedAt", null)
                .addSnapshotListener { snapshot, error ->

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

                        val name = doc.getString("name") ?: return@mapNotNull null

                        // ✅ DAS ist entscheidend
                        ShoppingItemEntity(
                            id = doc.id,
                            listId = listId,
                            name = name,
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

    override suspend fun addItem(listId: String, item: ShoppingItemEntity): Boolean {
        return try {

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
                        "updatedAt" to item.updatedAt,
                        "version" to item.updatedAt
                    ),
                    SetOptions.merge()
                )
                .await()

            true

        } catch (e: Exception) {
            Log.e("FIRESTORE", "addItem FAILED", e)
            false
        }
    }

    override suspend fun updateItem(listId: String, item: ShoppingItemEntity): Boolean {
        return try {

            itemsRef(listId)
                .document(item.id)
                .set(
                    mapOf(
                        "name" to item.name,
                        "quantity" to item.quantity,
                        "category" to item.category,
                        "isChecked" to item.isChecked,
                        "deletedAt" to item.deletedAt,
                        "updatedAt" to item.updatedAt,
                        "version" to item.updatedAt
                    ),
                    SetOptions.merge()
                )
                .await()

            true

        } catch (e: Exception) {
            Log.e("FIRESTORE", "updateItem FAILED", e)
            false
        }
    }

    override suspend fun deleteItem(listId: String, itemId: String): Boolean {
        return try {

            itemsRef(listId)
                .document(itemId)
                .update(
                    mapOf(
                        "deletedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()

            true

        } catch (e: Exception) {
            Log.e("FIRESTORE", "deleteItem FAILED", e)
            false
        }
    }

    override suspend fun createList(list: ShoppingListEntity, authUid: String): Boolean {
        return try {

            firestore
                .collection("lists")
                .document(list.id)
                .set(
                    mapOf(
                        "name" to list.name,
                        "ownerId" to authUid,
                        "storeTypes" to list.storeTypes.map { it.name },
                        "itemCount" to list.itemCount,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                        "sharedWith" to emptyList<String>(),
                        "deletedAt" to null
                    )
                )
                .await()

            true

        } catch (e: Exception) {
            Log.e("FIRESTORE", "createList FAILED", e)
            false
        }
    }

    override suspend fun addUserToList(
        listId: String,
        userId: String
    ) {
        firestore
            .collection("lists")
            .document(listId)
            .update(
                "sharedWith",
                FieldValue.arrayUnion(userId)
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
                ?.toDate()?.time ?: 0L

        val updatedAt =
            (get("updatedAt") as? Timestamp)
                ?.toDate()?.time ?: 0L

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

    suspend fun createInvite(
        listIds: List<String>,
        createdByName: String,
        ownerId: String
    ): String {

        val inviteId = UUID.randomUUID().toString()

        val data = mapOf(
            "listIds" to listIds,
            "createdByName" to createdByName,
            "ownerId" to ownerId, // 🔥 NEU
            "createdAt" to System.currentTimeMillis()
        )

        firestore
            .collection("invites")
            .document(inviteId)
            .set(data)
            .await()

        return inviteId
    }

    suspend fun getInviteData(inviteId: String): InviteData? {

        return try {

            val doc = firestore
                .collection("invites")
                .document(inviteId)
                .get()
                .await()

            if (!doc.exists()) return null

            val listIds = doc.get("listIds") as? List<String> ?: emptyList()
            val sender = doc.getString("createdByName") ?: "Unbekannt"
            val createdAt = doc.getLong("createdAt") ?: 0L
            val consumedAt = doc.getLong("consumedAt")

            InviteData(
                listIds = listIds,
                senderName = sender,
                createdAt = createdAt,
                consumedAt = consumedAt
            )

        } catch (e: Exception) {

            Log.e("INVITE", "Failed to load invite", e)
            null
        }
    }

    suspend fun getInviteListId(inviteId: String): String? {
        val snapshot = firestore
            .collection("invites")
            .document(inviteId)
            .get()
            .await()

        return snapshot.getString("listId")
    }

    suspend fun getListsForUser(userId: String): List<ShoppingListEntity> {
        val snapshot = firestore
            .collection("lists")
            .whereEqualTo("ownerId", userId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.toShoppingListEntity() }
    }

    override suspend fun markInviteConsumed(inviteId: String) {

        firestore
            .collection("invites")
            .document(inviteId)
            .update(
                "consumedAt",
                FieldValue.serverTimestamp()
            )
            .await()
    }

    suspend fun saveUserProfile(
        uid: String,
        firstName: String,
        lastName: String,
        email: String
    ) {
        val data = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore
            .collection("users")
            .document(uid)
            .set(data)
            .await()
    }

    suspend fun upsertUserProfile(
        uid: String,
        firstName: String?,
        lastName: String?,
        email: String?,
        profileName: String?
    ) {
        val updates = mutableMapOf<String, Any>(
            "updatedAt" to System.currentTimeMillis()
        )

        // 🔥 NUR setzen wenn sinnvoll
        if (!firstName.isNullOrBlank()) {
            updates["firstName"] = firstName
        }

        if (!lastName.isNullOrBlank()) {
            updates["lastName"] = lastName
        }

        if (!email.isNullOrBlank()) {
            updates["email"] = email
        }

        if (!profileName.isNullOrBlank()) {
            updates["profileName"] = profileName.trim()
        }

        firestore.collection("users")
            .document(uid)
            .set(updates, SetOptions.merge())
    }

    suspend fun getUserProfile(uid: String): Map<String, Any>? {
        return firestore.collection("users")
            .document(uid)
            .get()
            .await()
            .data
    }

    fun listenToUserProfile(
        uid: String,
        onChange: (Map<String, Any>?) -> Unit
    ): ListenerRegistration {

        return firestore.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("PROFILE", "Listener error", error)
                    return@addSnapshotListener
                }

                onChange(snapshot?.data)
            }
    }

    suspend fun deleteUserCompletely(uid: String) {

        val db = firestore

        // ============================================================
        // 1. USER
        // ============================================================
        db.collection("users")
            .document(uid)
            .delete()
            .await()

        // ============================================================
        // 2. REMOVE USER FROM ALL sharedWith ARRAYS
        // ============================================================
        val allLists = db.collection("lists")
            .get()
            .await()

        allLists.documents.forEach { doc ->
            val sharedWith = doc.get("sharedWith") as? List<*>
                ?: emptyList<Any>()

            if (uid in sharedWith) {
                try {
                    doc.reference.update(
                        "sharedWith",
                        FieldValue.arrayRemove(uid)
                    ).await()
                } catch (e: Exception) {
                    Log.e("DELETE", "Failed removing user from sharedWith ${doc.id}", e)
                }
            }
        }

        // ============================================================
        // 3. DELETE OWNED LISTS
        // ============================================================
        val lists = db.collection("lists")
            .whereEqualTo("ownerId", uid)
            .get()
            .await()

        lists.documents.forEach {
            it.reference.delete().await()
        }

        // ============================================================
        // 4. CLEAN LEGACY MEMBERSHIPS (optional)
        // ============================================================
        val memberships = db.collection("memberships")
            .whereEqualTo("userId", uid)
            .get()
            .await()

        memberships.documents.forEach {
            it.reference.delete().await()
        }
    }
}