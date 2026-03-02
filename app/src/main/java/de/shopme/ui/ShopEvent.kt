package de.shopme.ui

import android.content.Context
import de.shopme.data.ShoppingItemEntity

sealed class ShopEvent {

    // Core Item Events
    data class AddItem(val name: String) : ShopEvent()

    data class DeleteItem(val item: ShoppingItemEntity) : ShopEvent()

    data class UpdateItem(
        val item: ShoppingItemEntity,
        val newName: String
    ) : ShopEvent()

    data class ToggleItem(
        val item: ShoppingItemEntity
    ) : ShopEvent()

    data object UndoDelete : ShopEvent()

    data object ClearAll : ShopEvent()

    // Invite / Sharing
    data class CreateInvite(val context: Context) : ShopEvent()
}