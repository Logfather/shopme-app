package de.shopme.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class FirestoreShoppingRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private val currentUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: error("User must be authenticated")

    private val _currentListId = MutableStateFlow<String?>(null)
    val currentListId: StateFlow<String?> = _currentListId.asStateFlow()

    fun setActiveList(listId: String) {
        _currentListId.value = listId
    }

    private fun itemsRef(listId: String) =
        firestore.collection("lists")
            .document(listId)
            .collection("items")

    // ============================================================
    // USER
    // ============================================================

    suspend fun ensureUserDocument() {
        val userRef = firestore.collection("users").document(currentUid)
        val snapshot = userRef.get().await()
        if (!snapshot.exists()) {
            userRef.set(mapOf("createdAt" to FieldValue.serverTimestamp())).await()
        }
    }

    // ============================================================
    // DEFAULT LIST (SAFE)
    // ============================================================

    suspend fun createOrGetDefaultList(): String {

        val snapshot = firestore
            .collection("lists")
            .whereEqualTo("ownerId", currentUid)
            .get()
            .await()

        val listId = if (!snapshot.isEmpty) {
            snapshot.documents.first().id
        } else {
            val newListRef = firestore.collection("lists").document()

            firestore.runTransaction { tx ->
                tx.set(
                    newListRef,
                    mapOf(
                        "ownerId" to currentUid,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()

            newListRef.id
        }

        ensureMembershipForList(listId)

        return listId
    }

    suspend fun ensureMembershipForList(listId: String) {

        val memberRef = firestore
            .collection("lists")
            .document(listId)
            .collection("members")
            .document(currentUid)

        val snapshot = memberRef.get().await()

        if (!snapshot.exists()) {
            memberRef.set(
                mapOf(
                    "role" to "admin",
                    "joinedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun awaitMembership(listId: String) {
        val memberRef = firestore
            .collection("lists")
            .document(listId)
            .collection("members")
            .document(currentUid)

        repeat(10) {
            if (memberRef.get().await().exists()) return
            delay(100)
        }

        error("Membership not established")
    }

    // ============================================================
    // INVITES
    // ============================================================

    suspend fun createInvite(listId: String): String {

        val inviteRef = firestore
            .collection("lists")
            .document(listId)
            .collection("invites")
            .document()

        inviteRef.set(
            mapOf(
                "createdBy" to currentUid,
                "createdAt" to FieldValue.serverTimestamp(),
                "consumed" to false,
                "role" to "editor"
            )
        ).await()

        return inviteRef.id
    }

    // ============================================================
    // OBSERVE ITEMS
    // ============================================================

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

                            val items = snapshot?.documents?.mapNotNull { doc ->
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

    // ============================================================
    // ITEMS
    // ============================================================

    suspend fun addItem(item: ShoppingItemEntity) {

        val listId = _currentListId.value ?: return

        itemsRef(listId)
            .document(item.id)
            .set(
                mapOf(
                    "name" to item.name,
                    "quantity" to item.quantity,
                    "category" to item.category,
                    "isChecked" to item.isChecked,
                    "deletedAt" to null,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "version" to 0
                )
            ).await()
    }

    suspend fun updateItem(item: ShoppingItemEntity) {

        val listId = _currentListId.value ?: return
        val docRef = itemsRef(listId).document(item.id)

        firestore.runTransaction { tx ->
            val snapshot = tx.get(docRef)
            val currentVersion = snapshot.getLong("version") ?: 0

            tx.update(
                docRef,
                mapOf(
                    "name" to item.name,
                    "quantity" to item.quantity,
                    "category" to item.category,
                    "isChecked" to item.isChecked,
                    "version" to currentVersion + 1,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }.await()
    }

    suspend fun softDelete(itemId: String) {

        val listId = _currentListId.value ?: return
        val docRef = itemsRef(listId).document(itemId)

        firestore.runTransaction { tx ->
            val snapshot = tx.get(docRef)
            val currentVersion = snapshot.getLong("version") ?: 0

            tx.update(
                docRef,
                mapOf(
                    "deletedAt" to FieldValue.serverTimestamp(),
                    "version" to currentVersion + 1,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }.await()
    }

    suspend fun clearAll() {

        val listId = _currentListId.value ?: return

        val snapshot = itemsRef(listId)
            .whereEqualTo("deletedAt", null)
            .get()
            .await()

        firestore.runTransaction { tx ->
            snapshot.documents.forEach { doc ->
                val currentVersion = doc.getLong("version") ?: 0
                tx.update(
                    doc.reference,
                    mapOf(
                        "deletedAt" to FieldValue.serverTimestamp(),
                        "version" to currentVersion + 1,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
        }.await()
    }
}