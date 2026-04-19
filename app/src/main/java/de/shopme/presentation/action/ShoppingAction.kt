package de.shopme.presentation.action

import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.model.StoreType
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.presentation.event.ShopEvent




sealed class ShoppingAction {

    object StartMultiStoreCreation : ShoppingAction()


    data class ToggleItem(
        val item: ShoppingItem,
        val newChecked: Boolean
    ) : ShoppingAction()

    data class ToggleStore(
        val store: StoreType
    ) : ShoppingAction()

    data class ConfirmStores(

        val customLists: List<String>

    ) : ShoppingAction()

    object CancelMultiCreation : ShoppingAction()

    object DeleteAllLists : ShoppingAction()



    // -----------------------------
    // USER / PROFILE EVENTS
    // -----------------------------

    data class LoadUserProfile(
        val uid: String
    ) : ShoppingAction()

    data class UserProfileLoaded(
        val uid: String,
        val profileName: String?,
        val firstName: String?,
        val lastName: String?,
        val email: String?,
        val exists: Boolean
    ) : ShoppingAction()

    data class UpdateUserProfile(
        val uid: String,
        val nickName: String,
        val firstName: String?,
        val lastName: String?,
        val email: String?
    ) : ShoppingAction()

}