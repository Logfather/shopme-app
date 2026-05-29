package de.shopme.testing.fake

import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryFakeFirestoreGateway : FirestoreGateway {

    var isNetworkAvailable = true

    // ============================================================
    // REMOTE STATE
    // ============================================================

    private val remoteLists =
        mutableMapOf<String, ShoppingListEntity>()

    private val remoteItems =
        mutableMapOf<String, MutableMap<String, ShoppingItemEntity>>()

    // ============================================================
    // FLOWS
    // ============================================================

    private val itemsFlow =
        MutableStateFlow<Map<String, MutableMap<String, ShoppingItemEntity>>>(
            emptyMap()
        )

    private val listsFlow =
        MutableStateFlow<Map<String, ShoppingListEntity>>(
            emptyMap()
        )

    override fun observeListsForUser(
        userId: String
    ): Flow<List<ShoppingListEntity>> {

        return listsFlow
            .map { it.values.toList() }
    }

    // ============================================================
    // NETWORK SIMULATION
    // ============================================================

    private fun ensureNetwork() {

        if (!isNetworkAvailable) {

            throw IllegalStateException(
                "Simulated offline mode"
            )
        }
    }

    // ============================================================
    // LIST
    // ============================================================

    override suspend fun createList(
        list: ShoppingListEntity,
        authUid: String
    ): Boolean {

        ensureNetwork()

        remoteLists[list.id] = list

        listsFlow.value = remoteLists.toMap()

        return true
    }

    override suspend fun softDeleteList(listId: String) {

        ensureNetwork()

        val current = remoteLists[listId] ?: return

        remoteLists[listId] = current.copy(
            deletedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        listsFlow.value = remoteLists.toMap()
    }

    // ============================================================
    // ITEMS
    // ============================================================

    override suspend fun addItem(
        listId: String,
        item: ShoppingItemEntity
    ): Boolean {

        ensureNetwork()

        val listItems =
            remoteItems.getOrPut(listId) { mutableMapOf() }

        listItems[item.id] = item

        itemsFlow.value =
            remoteItems.mapValues { entry ->
                entry.value.toMutableMap()
            }

        return true
    }

    override suspend fun updateItem(
        listId: String,
        item: ShoppingItemEntity
    ): Boolean {

        ensureNetwork()

        val listItems =
            remoteItems.getOrPut(listId) { mutableMapOf() }

        listItems[item.id] = item

        itemsFlow.value =
            remoteItems.mapValues { entry ->
                entry.value.toMutableMap()
            }

        return true
    }

    override suspend fun deleteItem(
        listId: String,
        itemId: String
    ): Boolean {

        ensureNetwork()

        val listItems = remoteItems[listId] ?: return false

        val current = listItems[itemId] ?: return false

        listItems[itemId] = current.copy(
            deletedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        itemsFlow.value =
            remoteItems.mapValues { entry ->
                entry.value.toMutableMap()
            }

        return true
    }

    // ============================================================
    // MEMBERSHIP
    // ============================================================

    override suspend fun addMembership(
        userId: String,
        listId: String
    ) {
        // no-op
    }

    override suspend fun addUserToList(
        listId: String,
        userId: String
    ) {
        // no-op
    }

    override suspend fun isUserMemberOfList(
        userId: String,
        listId: String
    ): Boolean {
        return true
    }

    // ============================================================
    // INVITES
    // ============================================================

    override suspend fun markInviteConsumed(
        inviteId: String
    ) {
        // no-op
    }

    // ============================================================
    // VERSIONING
    // ============================================================

    override suspend fun getItemVersion(
        listId: String,
        itemId: String
    ): Long? {

        return remoteItems[listId]
            ?.get(itemId)
            ?.updatedAt
    }

    // ============================================================
    // OBSERVE
    // ============================================================

    override fun observeItems(
        listId: String
    ): Flow<List<ShoppingItemEntity>> {

        return itemsFlow.map { all ->

            all[listId]
                ?.values
                ?.sortedBy { it.createdAt }
                ?: emptyList()
        }
    }

    override fun observeListById(
        listId: String
    ): Flow<ShoppingListEntity?> {

        return listsFlow.map { all ->
            all[listId]
        }
    }

    // ============================================================
    // REMOVE USER
    // ============================================================

    override suspend fun removeUserFromList(
        listId: String,
        userId: String
    ) {
        // no-op
    }

    // ============================================================
    // GET ITEM
    // ============================================================

    override suspend fun getItem(
        listId: String,
        itemId: String
    ): ShoppingItemEntity? {

        return remoteItems[listId]
            ?.get(itemId)
    }
}