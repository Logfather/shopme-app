package de.shopme.presentation.undo

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
}