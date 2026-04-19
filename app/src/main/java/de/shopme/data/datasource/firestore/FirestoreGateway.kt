package de.shopme.data.datasource.firestore

import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import kotlinx.coroutines.flow.Flow

interface FirestoreGateway {

    // LIST
    suspend fun createList(list: ShoppingListEntity, authUid: String): Boolean
    suspend fun softDeleteList(listId: String)

    // ITEMS
    suspend fun addItem(listId: String, item: ShoppingItemEntity): Boolean
    suspend fun updateItem(listId: String, item: ShoppingItemEntity): Boolean
    suspend fun deleteItem(listId: String, itemId: String): Boolean

    // MEMBERSHIP
    suspend fun addMembership(userId: String, listId: String)
    suspend fun addUserToList(listId: String, userId: String)
    suspend fun isUserMemberOfList(userId: String, listId: String): Boolean

    // INVITE
    suspend fun markInviteConsumed(inviteId: String)

    // READ
    suspend fun getItemVersion(listId: String, itemId: String): Long?
    fun observeItems(listId: String): Flow<List<ShoppingItemEntity>>
    fun observeListById(listId: String): Flow<ShoppingListEntity?>

    suspend fun removeUserFromList(listId: String, userId: String)
}