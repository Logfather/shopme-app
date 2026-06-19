package de.shopme.testing.system.fake

import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.domain.model.InviteData
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeFirestoreGateway : FirestoreGateway {

    var shouldFailWrites = false

    var addItemCallCount = 0

    private val items =
        mutableMapOf<String, MutableMap<String, ShoppingItemEntity>>()

    override suspend fun addMembership(
        userId: String,
        listId: String
    ) {
        // no-op
    }

    override suspend fun isUserMemberOfList(
        userId: String,
        listId: String
    ): Boolean {
        return true
    }

    override suspend fun removeUserFromList(
        listId: String,
        userId: String
    ) {
        // no-op
    }

    override fun observeListById(
        listId: String
    ): Flow<ShoppingListEntity?> {

        return flowOf(null)
    }

    override suspend fun softDeleteList(
        listId: String
    ) {
        // no-op
    }

    override suspend fun getItemVersion(
        listId: String,
        itemId: String
    ): Long? {
        return null
    }

    override suspend fun getItem(
        listId: String,
        itemId: String
    ): ShoppingItemEntity? {

        return items[listId]?.get(itemId)
    }

    override fun observeItems(
        listId: String
    ): Flow<List<ShoppingItemEntity>> {

        return flowOf(emptyList())
    }

    override suspend fun addItem(
        listId: String,
        item: ShoppingItemEntity
    ): Boolean {

        addItemCallCount++

        if (shouldFailWrites) {
            throw RuntimeException("Forced test failure")
        }

        val listItems =
            items.getOrPut(listId) {
                mutableMapOf()
            }

        listItems[item.id] = item

        return true
    }

    override suspend fun updateItem(
        listId: String,
        item: ShoppingItemEntity
    ): Boolean {

        if (shouldFailWrites) {
            throw RuntimeException("Forced test failure")
        }

        items[listId]?.set(item.id, item)

        return true
    }

    override suspend fun deleteItem(
        listId: String,
        itemId: String
    ): Boolean {

        if (shouldFailWrites) {
            throw RuntimeException("Forced test failure")
        }

        items[listId]?.remove(itemId)

        return true
    }

    override suspend fun createList(
        list: ShoppingListEntity,
        authUid: String
    ): Boolean {

        if (shouldFailWrites) {
            throw RuntimeException("Forced test failure")
        }

        return true
    }

    override fun observeListsForUser(
        userId: String
    ): Flow<List<ShoppingListEntity>> {

        return flowOf(emptyList())
    }

    override suspend fun addUserToList(
        listId: String,
        userId: String
    ) {
        // no-op
    }

    override suspend fun markInviteConsumed(
        inviteId: String
    ) {
        // no-op
    }

    override suspend fun createInvite(
        listIds: List<String>,
        createdByName: String,
        ownerId: String
    ): String {

        return "fake-invite-id"
    }

    override suspend fun getInviteData(
        inviteId: String
    ): InviteData? {

        return null
    }

    override suspend fun createInviteLink(
        listId: String,
        createdByName: String,
        ownerId: String
    ): String {

        return "fake-link"
    }

    override suspend fun getListOnce(
        listId: String
    ): ShoppingListEntity? {

        return null
    }

    override suspend fun upsertUserProfile(
        uid: String,
        firstName: String?,
        lastName: String?,
        email: String?,
        profileName: String?
    ) {
        // no-op
    }

    override fun listenToUserProfile(
        uid: String,
        onChange: (Map<String, Any>?) -> Unit
    ) = null

    override suspend fun saveUserProfile(
        uid: String,
        firstName: String,
        lastName: String,
        email: String
    ) {
        // no-op
    }

    override suspend fun getUserProfile(
        uid: String
    ): Map<String, Any>? {

        return null
    }
}