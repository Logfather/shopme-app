package de.shopme.test.fakes

import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import kotlinx.coroutines.flow.flowOf

class FakeFirestoreDataSource : FirestoreGateway {

    var failMode: String? = null

    private val lists = mutableMapOf<String, ShoppingListEntity>()

    // ============================================================
    // LISTS
    // ============================================================

    override suspend fun createList(
        list: ShoppingListEntity,
        authUid: String
    ): Boolean {
        maybeFail()
        return true
    }

    override suspend fun addMembership(userId: String, listId: String) {
        maybeFail()
    }

    override suspend fun softDeleteList(listId: String) {
        maybeFail()
    }

    // ============================================================
    // ITEMS
    // ============================================================

    override suspend fun addItem(
        listId: String,
        item: ShoppingItemEntity
    ): Boolean {
        maybeFail()
        return true
    }

    override suspend fun updateItem(
        listId: String,
        item: ShoppingItemEntity
    ): Boolean {
        maybeFail()
        return true
    }

    override suspend fun deleteItem(
        listId: String,
        itemId: String
    ): Boolean {
        maybeFail()
        return true
    }

    override suspend fun getItemVersion(
        listId: String,
        itemId: String
    ): Long? = null

    // ============================================================
    // MEMBERSHIP
    // ============================================================

    override suspend fun isUserMemberOfList(
        userId: String,
        listId: String
    ): Boolean = true

    override suspend fun addUserToList(
        listId: String,
        userId: String
    ) {
        maybeFail()
    }

    override suspend fun removeUserFromList(listId: String, userId: String) {
        val list = lists[listId] ?: return

        val updated = list.copy(
            sharedWith = list.sharedWith.filter { it != userId }
        )

        lists[listId] = updated
    }

    // ============================================================
    // INVITE
    // ============================================================

    override suspend fun markInviteConsumed(inviteId: String) {
        maybeFail()
    }

    // ============================================================
    // OBSERVE
    // ============================================================

    override fun observeListById(listId: String) =
        flowOf<ShoppingListEntity?>(null)

    override fun observeItems(listId: String) =
        flowOf(emptyList<ShoppingItemEntity>())

    // ============================================================
    // INTERNAL
    // ============================================================

    private fun maybeFail() {
        when (failMode) {
            "PERMISSION" -> throw RuntimeException("PERMISSION")
            "NETWORK" -> throw RuntimeException("NETWORK")
        }
    }
}