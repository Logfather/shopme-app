package de.shopme.presentation.event

import android.content.Context
import de.shopme.domain.model.ShoppingItem

sealed interface ShopEvent {

    // -----------------------------
    // ITEM EVENTS
    // -----------------------------

    sealed interface Item : ShopEvent {

        data class Add(
            val name: String
        ) : Item

        data class Toggle(
            val item: ShoppingItem
        ) : Item

        data class Delete(
            val item: ShoppingItem
        ) : Item

        data class Update(
            val item: ShoppingItem,
            val newName: String
        ) : Item

        data class SetChecked(
            val item: ShoppingItem,
            val checked: Boolean
        ) : Item

        data class RetrySync(
            val itemId: String
        ) : Item
    }

    // -----------------------------
    // LIST EVENTS
    // -----------------------------

    sealed interface List : ShopEvent {

        object ClearAll : List

        object DeleteAllLists : List

        data class Delete(val listId: String) : List

        object UndoLastAction : List
    }

    // -----------------------------
    // SPEECH EVENTS
    // -----------------------------

    sealed interface Speech : ShopEvent {

        data class AddItemFromSpeech(
            val text: String
        ) : Speech
    }

    // -----------------------------
    // SYSTEM EVENTS
    // -----------------------------

    sealed interface System : ShopEvent {
        object CreateInvite : System
    }
}