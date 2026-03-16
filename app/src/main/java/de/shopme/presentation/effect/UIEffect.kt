package de.shopme.presentation.effect

import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType

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

    data class DeleteList(
        val list: ShoppingListEntity
    ) : UIEffect()

}