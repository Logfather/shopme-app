package de.shopme.presentation.undo

import de.shopme.domain.model.ListDeleteSnapshot
import de.shopme.domain.model.ShoppingItem

sealed class UndoAction {

    data class DeleteItem(
        val item: ShoppingItem
    ) : UndoAction()

    data class ToggleItem(
        val item: ShoppingItem
    ) : UndoAction()

    data class UpdateItem(
        val oldItem: ShoppingItem
    ) : UndoAction()

    // 🔥 NEU
    data class DeleteList(
        val snapshot: ListDeleteSnapshot
    ) : UndoAction()

    data class AddItem(val name: String) : UndoAction()
}