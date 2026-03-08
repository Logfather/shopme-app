package de.shopme.presentation.event

import android.content.Context
import de.shopme.domain.model.ShoppingItem

sealed class ShopEvent {

    data class AddItem(
        val name: String
    ) : ShopEvent()

    data class ToggleItem(
        val item: ShoppingItem
    ) : ShopEvent()

    data class DeleteItem(
        val item: ShoppingItem
    ) : ShopEvent()

    data class UpdateItem(
        val item: ShoppingItem,
        val newName: String
    ) : ShopEvent()

    data class CreateInvite(
        val context: Context
    ) : ShopEvent()

    object ClearAll : ShopEvent()
}