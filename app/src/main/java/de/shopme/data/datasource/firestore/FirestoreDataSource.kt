package de.shopme.data.datasource.firestore

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.Timestamp
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreDataSource {

    private val firestore = FirebaseFirestore.getInstance()

    private fun requireUid(): String =
        FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")

    // ============================================================
    // LISTS
    // ============================================================

    suspend fun addMembership(userId: String, listId: String) {

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

    suspend fun isUserMemberOfList(
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

    fun observeListById(listId: String): Flow<ShoppingListEntity?> = callbackFlow {

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
                        close(error)
                        return@addSnapshotListener
                    }

                    val lists = snapshot?.documents
                        ?.mapNotNull { it.toShoppingListEntity() }
                        ?: emptyList()

                    trySend(lists).isSuccess
                }

            awaitClose { listener.remove() }
        }

        val sharedFlow = callbackFlow {
            val listener = firestore
                .collection("lists")
                .whereArrayContains("sharedWith", uid)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val lists = snapshot?.documents
                        ?.mapNotNull { it.toShoppingListEntity() }
                        ?.filter { uid in it.sharedWith }
                        ?: emptyList()

                    trySend(lists).isSuccess
                }

            awaitClose { listener.remove() }
        }

        return combine(ownedFlow, sharedFlow) { owned, shared ->
            (owned + shared).distinctBy { it.id }
        }
    }

    suspend fun softDeleteList(listId: String) {
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
                    close(error)
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

    suspend fun getItemVersion(
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

    fun observeItems(listId: String): Flow<List<ShoppingItemEntity>> =
        callbackFlow {

            val listener = itemsRef(listId)
                .whereEqualTo("deletedAt", null)
                .addSnapshotListener { snapshot, error ->


                    val items = snapshot?.documents?.mapNotNull { doc ->

                        val createdAt =
                            (doc.get("createdAt") as? Timestamp)
                                ?.toDate()?.time ?: 0L

                        val updatedAt =
                            (doc.get("updatedAt") as? Timestamp)
                                ?.toDate()?.time ?: 0L

                        val deletedAt =
                            (doc.get("deletedAt") as? Timestamp)
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

                    trySend(items).isSuccess
                }

            awaitClose { listener.remove() }
        }

    // ============================================================
    // WRITE OPERATIONS
    // ============================================================

    suspend fun addItem(listId: String, item: ShoppingItemEntity) {
        itemsRef(listId)
            .document(item.id)
            .set(
                mapOf(
                    "name" to item.name,
                    "quantity" to item.quantity,
                    "category" to item.category,
                    "isChecked" to item.isChecked,
                    "deletedAt" to item.deletedAt,

                    // 🔥 CRITICAL FIXES
                    "createdAt" to item.createdAt,
                    "updatedAt" to item.updatedAt,

                    // optional:
                    "version" to item.updatedAt
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
            .await()
    }

    suspend fun updateItem(listId: String, item: ShoppingItemEntity) {
        itemsRef(listId)
            .document(item.id)
            .set(
                mapOf(
                    "name" to item.name,
                    "quantity" to item.quantity,
                    "category" to item.category,
                    "isChecked" to item.isChecked,
                    "deletedAt" to item.deletedAt,

                    // 🔥 CRITICAL
                    "updatedAt" to item.updatedAt,

                    // optional:
                    "version" to item.updatedAt
                ),
                com.google.firebase.firestore.SetOptions.merge()
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
            )
            .await()
    }

    suspend fun createList(list: ShoppingListEntity, authUid: String) {

        firestore
            .collection("lists")
            .document(list.id)
            .set(
                mapOf(
                    "name" to list.name,
                    "ownerId" to authUid, // 🔥 FIX
                    "storeTypes" to list.storeTypes.map { it.name },
                    "itemCount" to list.itemCount,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
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
        createdByName: String
    ): String {

        val inviteId = UUID.randomUUID().toString()

        val data = mapOf(
            "listIds" to listIds,
            "createdByName" to createdByName,
            "createdAt" to System.currentTimeMillis()
        )

        firestore
            .collection("invites")
            .document(inviteId)
            .set(data)
            .await()

        return inviteId
    }

    suspend fun getInviteData(inviteId: String): Pair<List<String>, String>? {

        return try {

            val doc = firestore
                .collection("invites")
                .document(inviteId)
                .get()
                .await()

            if (!doc.exists()) return null

            val listIds = doc.get("listIds") as? List<String> ?: emptyList()
            val sender = doc.getString("createdByName") ?: "Unbekannt"

            listIds to sender

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

    fun observeMembership(
        userId: String,
        listId: String
    ): Flow<Boolean> = callbackFlow {

        val membershipId = "${userId}_${listId}"

        val listener = firestore
            .collection("list_members")
            .document(membershipId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("MEMBERSHIP_FLOW", "Listener error", error)
                    return@addSnapshotListener   // 🔥 NICHT close(error)
                }

                val exists = snapshot?.exists() == true

                Log.d("MEMBERSHIP_FLOW", "Membership update → $membershipId exists=$exists")

                trySend(exists).isSuccess
            }

        awaitClose { listener.remove() }
    }

    fun observeMemberships(userId: String): Flow<List<String>> = callbackFlow {

        Log.d("MEMBERSHIP", "Start listening for user: $userId")

        var isInitial = true
        var lastEmitted: Set<String> = emptySet()

        val listener = firestore
            .collection("list_members")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val current = snapshot?.documents
                    ?.mapNotNull { it.getString("listId") }
                    ?.toSet()
                    ?: emptySet()

                // 🔥 1. Initial Snapshot skippen
                if (isInitial) {
                    Log.d("MEMBERSHIP", "Initial snapshot → skip")
                    lastEmitted = current
                    isInitial = false
                    return@addSnapshotListener
                }

                // 🔥 2. Nur echte Änderungen
                if (current == lastEmitted) {
                    return@addSnapshotListener
                }

                // 🔥 3. Diff berechnen
                val added = current - lastEmitted

                Log.d("SYNC_MEMBERSHIP", "Membership update: $current")

                lastEmitted = current

                // 🔥 4. Nur wenn wirklich neue Listen dazu kamen
                if (added.isNotEmpty()) {
                    trySend(current.toList()).isSuccess
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun getUserListIds(userId: String): List<String> {

        return try {

            val snapshot = firestore
                .collection("lists")
                .whereArrayContains("sharedWith", userId)
                .get()
                .await()

            val result = snapshot.documents.mapNotNull { it.id }

            Log.d("MEMBERSHIP_FETCH", "Fetched memberships (sharedWith) = $result")

            result

        } catch (e: Exception) {

            Log.e("MEMBERSHIP_FETCH", "Failed to fetch memberships", e)

            emptyList()
        }
    }
}