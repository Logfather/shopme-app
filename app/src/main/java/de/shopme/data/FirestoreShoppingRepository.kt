package de.shopme.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class FirestoreShoppingRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private val _currentListId = MutableStateFlow<String?>(null)
    val currentListId: StateFlow<String?> = _currentListId.asStateFlow()

    fun setActiveList(listId: String) {
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
        isCustom: Boolean = false
    ): String{

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
            )

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

                        ShoppingListEntity(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            ownerId = document.getString("ownerId") ?: "",
                            storeTypes = storeTypes,
                            isCustom = document.getBoolean("isCustom") ?: false
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

    fun observeItems(): Flow<List<ShoppingItemEntity>> =
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
                                    ShoppingItemEntity(
                                        id = doc.id,
                                        name = doc.getString("name") ?: return@mapNotNull null,
                                        quantity = (doc.getLong("quantity") ?: 1).toInt(),
                                        category = doc.getString("category") ?: "Sonstiges",
                                        isChecked = doc.getBoolean("isChecked") ?: false,
                                        deletedAt = doc.getTimestamp("deletedAt"),
                                        createdAt = doc.getTimestamp("createdAt"),
                                        updatedAt = doc.getTimestamp("updatedAt"),
                                        version = (doc.getLong("version") ?: 0).toInt()
                                    )
                                } ?: emptyList()

                            trySend(items)
                        }

                    awaitClose { listener.remove() }
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

    suspend fun updateItem(item: ShoppingItemEntity) {

        val listId = _currentListId.value ?: return

        itemsRef(listId)
            .document(item.id)
            .update(
                mapOf(
                    "name" to item.name,
                    "quantity" to item.quantity,
                    "category" to item.category,
                    "isChecked" to item.isChecked,
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "version" to item.version + 1
                )
            ).await()
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