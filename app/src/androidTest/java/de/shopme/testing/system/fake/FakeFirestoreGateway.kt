package de.shopme.testing.system.fake

import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeFirestoreGateway : FirestoreGateway {

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
        return null
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

        return true
    }

    override suspend fun updateItem(
        listId: String,
        item: ShoppingItemEntity
    ): Boolean {

        return true
    }

    override suspend fun deleteItem(
        listId: String,
        itemId: String
    ): Boolean {

        return true
    }

    override suspend fun createList(
        list: ShoppingListEntity,
        authUid: String
    ): Boolean {

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
}