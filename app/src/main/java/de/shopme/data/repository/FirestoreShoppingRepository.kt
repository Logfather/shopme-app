package de.shopme.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.shopme.data.mapper.EntityMapper.toDomain
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import de.shopme.data.mapper.ShopMapper
import de.shopme.domain.model.ShoppingItem

class FirestoreShoppingRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private val _currentListId = MutableStateFlow<String?>(null)
    val currentListId: StateFlow<String?> = _currentListId.asStateFlow()

    fun setActiveList(listId: String) {
        if (_currentListId.value == listId) return
        _currentListId.value = listId
    }

    // ============================================================
    // HELPER
    // ============================================================

    private fun requireUid(): String =
        FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")

    // ============================================================
    // LISTS
    // ============================================================

    suspend fun deleteList(listId: String) {
        firestore.collection("lists")
            .document(listId)
            .delete()
            .await()
    }

    suspend fun createList(
        name: String,
        storeTypes: List<StoreType>,
        isCustom: Boolean
    ): String {

        val uid = requireUid()

        val newListRef =
            firestore.collection("lists").document()

        try {
            newListRef.set(
                mapOf(
                    "name" to name,
                    "ownerId" to uid,
                    "storeTypes" to storeTypes.map { it.name },
                    "isCustom" to isCustom,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

        } catch (e: Exception) {
            Log.e("CREATE_DEBUG", "Create list failed", e)
            throw e
        }

        return newListRef.id
    }

    fun observeListsForUser(): Flow<List<ShoppingListEntity>> =
        callbackFlow {

            val uid = requireUid()

            val listener = firestore
                .collection("lists")
                .whereEqualTo("ownerId", uid)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val lists = snapshot?.documents?.mapNotNull { document ->

                        val storeTypes =
                            (document.get("storeTypes") as? List<String>)
                                ?.mapNotNull {
                                    runCatching { StoreType.valueOf(it) }
                                        .getOrNull()
                                }
                                ?: emptyList()

                        val createdAt =
                            document.getTimestamp("createdAt")
                                ?.toDate()
                                ?.time ?: System.currentTimeMillis()

                        val updatedAt =
                            document.getTimestamp("updatedAt")
                                ?.toDate()
                                ?.time ?: System.currentTimeMillis()

                        ShoppingListEntity(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            ownerId = document.getString("ownerId") ?: "",
                            storeTypes = storeTypes,
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

    fun observeItems(): Flow<List<ShoppingItem>> =
        _currentListId
            .filterNotNull()
            .flatMapLatest { listId ->
                callbackFlow {

                    val listener = itemsRef(listId)
                        .whereEqualTo("deletedAt", null)
                        .addSnapshotListener { snapshot, error ->

                            if (error != null) {
                                close(error)
                                return@addSnapshotListener
                            }

                            val items =
                                snapshot?.documents?.mapNotNull { doc ->

                                    val createdAt =
                                        doc.getTimestamp("createdAt")
                                            ?.toDate()
                                            ?.time ?: System.currentTimeMillis()

                                    val updatedAt =
                                        doc.getTimestamp("updatedAt")
                                            ?.toDate()
                                            ?.time ?: System.currentTimeMillis()

                                    val deletedAt =
                                        doc.getTimestamp("deletedAt")
                                            ?.toDate()
                                            ?.time

                                    ShoppingItemEntity(
                                        id = doc.id,
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

                    // ENTITY → DOMAIN
                    .map { entities ->
                        entities.map(ShopMapper::fromEntity)
                    }
            }

    suspend fun addItem(item: ShoppingItemEntity) {

        val listId = _currentListId.value ?: return
        val uid = requireUid()

        itemsRef(listId)
            .document(item.id)
            .set(
                mapOf(
                    "name" to item.name,
                    "quantity" to item.quantity,
                    "category" to item.category,
                    "isChecked" to item.isChecked,
                    "deletedAt" to null,
                    "ownerId" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "version" to 0
                )
            ).await()
    }

    suspend fun updateItem(
        item: ShoppingItemEntity
    ) {

        val listId = _currentListId.value ?: return

        val docRef =
            itemsRef(listId).document(item.id)

        var attempt = 0
        val maxRetries = 3

        while (attempt < maxRetries) {

            try {

                firestore.runTransaction { transaction ->

                    val snapshot =
                        transaction.get(docRef)

                    val serverVersion =
                        (snapshot.getLong("version") ?: 0).toInt()

                    // ------------------------------
                    // CONFLICT DETECTION
                    // ------------------------------
                    if (serverVersion != item.version) {
                        throw IllegalStateException("Conflict detected")
                    }

                    // ------------------------------
                    // UPDATE
                    // ------------------------------
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

                return

            } catch (e: Exception) {

                attempt++

                if (attempt >= maxRetries) {
                    throw e
                }

                // kurzer Backoff
                kotlinx.coroutines.delay(50L * attempt)
            }
        }
    }

    suspend fun softDelete(itemId: String) {

        val listId = _currentListId.value ?: return

        itemsRef(listId)
            .document(itemId)
            .update(
                mapOf(
                    "deletedAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    suspend fun clearAll() {

        val listId = _currentListId.value ?: return

        val snapshot = itemsRef(listId)
            .whereEqualTo("deletedAt", null)
            .get()
            .await()

        snapshot.documents.forEach { doc ->
            doc.reference.update(
                "deletedAt",
                FieldValue.serverTimestamp()
            )
        }
    }

    suspend fun createInvite(listId: String): String {

        val uid = requireUid()

        val inviteRef = firestore
            .collection("lists")
            .document(listId)
            .collection("invites")
            .document()

        inviteRef.set(
            mapOf(
                "createdBy" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "consumed" to false,
                "role" to "editor"
            )
        ).await()

        return inviteRef.id
    }

    // ============================================================
    // MEMBERSHIP
    // ============================================================

    suspend fun ensureUserDocument() { /* unverändert */ }
}