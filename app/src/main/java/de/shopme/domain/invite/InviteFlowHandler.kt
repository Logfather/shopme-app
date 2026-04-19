package de.shopme.domain.invite

import android.util.Log
import de.shopme.data.datasource.firestore.FirestoreDataSource
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.domain.model.ShoppingList
import de.shopme.presentation.state.ShoppingState


data class InviteResult(
    val listIds: List<String>,
    val senderName: String,
    val createdAt: Long,
    val consumedAt: Long?
)
class InviteFlowHandler(
    private val firestoreDataSource: FirestoreDataSource,
    private val roomRepository: RoomShoppingRepository
) {

    suspend fun loadInvite(inviteId: String): InviteResult? {
        return try {
            val data = firestoreDataSource.getInviteData(inviteId) ?: return null

            InviteResult(
                listIds = data.listIds,
                senderName = data.senderName,
                createdAt = data.createdAt,
                consumedAt = data.consumedAt
            )
        } catch (e: Exception) {
            Log.e("INVITE", "Failed to load invite", e)
            null
        }
    }

    fun isExpired(createdAt: Long): Boolean {
        val now = System.currentTimeMillis()
        val expiry = 24 * 60 * 60 * 1000L
        return (now - createdAt) > expiry
    }

    fun resolveLists(
        state: ShoppingState,
        listIds: List<String>
    ): List<ShoppingList> {

        val currentLists = state.lists

        return listIds.mapNotNull { id ->
            currentLists.find { it.id == id }
        }
    }
}