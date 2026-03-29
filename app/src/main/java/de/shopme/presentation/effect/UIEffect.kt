package de.shopme.presentation.effect

import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType
import de.shopme.presentation.undo.UndoAction

sealed class UIEffect {

    data class ShowSnackbar(
        val message: String
    ) : UIEffect()

    object ShowWelcomeDialog : UIEffect()

    object HideWelcomeDialog : UIEffect()

    // ------------------------------------------------------------
    // LIST EFFECTS
    // ------------------------------------------------------------

    data class CreateLists(
        val stores: List<StoreType>,
        val customLists: List<String>
    ) : UIEffect()

    data class DeleteList(val listId: String) : UIEffect()

    data class AddItem(
        val name: String
    ) : UIEffect()

    data class ToggleItem(
        val item: de.shopme.domain.model.ShoppingItem
    ) : UIEffect()

    data class DeleteItem(
        val item: de.shopme.domain.model.ShoppingItem
    ) : UIEffect()

    data class RetrySync(val itemId: String) : UIEffect()

    data class ShowUndo(
        val action: UndoAction,
        val message: String
    ) : UIEffect()

    data class UpdateItem(
        val item: ShoppingItem,
        val newName: String
    ) : UIEffect()

    object DeleteAllLists : UIEffect()

}