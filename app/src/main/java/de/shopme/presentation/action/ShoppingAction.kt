package de.shopme.presentation.action

import de.shopme.domain.model.StoreType
import de.shopme.domain.model.ShoppingListEntity

sealed class ShoppingAction {

    object StartMultiStoreCreation : ShoppingAction()

    data class ToggleStore(
        val store: StoreType
    ) : ShoppingAction()

    data class ConfirmStores(

        val customLists: List<String>

    ) : ShoppingAction()

    object CancelMultiCreation : ShoppingAction()

    data class EditList(
        val list: ShoppingListEntity
    ) : ShoppingAction()

    data class DeleteList(
        val list: ShoppingListEntity
    ) : ShoppingAction()

}