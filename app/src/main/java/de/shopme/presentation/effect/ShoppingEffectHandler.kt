package de.shopme.presentation.effect

import android.util.Log
import de.shopme.domain.auth.AuthProvider
import de.shopme.domain.item.ItemActionHandler
import de.shopme.presentation.viewmodel.ShoppingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ShoppingEffectHandler(
    private val authProvider: AuthProvider,
    private val viewModel: ShoppingViewModel,
    private val scope: CoroutineScope,
    private val itemActionHandler: ItemActionHandler
) {

    fun handle(effect: UIEffect) {

        when (effect) {

            is UIEffect.AddItem -> {
                Log.d("EFFECT_DEBUG", "AddItem effect: ${effect.name}")

                scope.launch {
                    val listId = viewModel.currentListId.value ?: return@launch
                    itemActionHandler.addItem(effect.name, listId)
                }
            }

            is UIEffect.UpdateItem -> {
                scope.launch {
                    itemActionHandler.updateItem(effect.item, effect.newName)
                }
            }

            is UIEffect.DeleteItem -> {
                scope.launch {
                    itemActionHandler.deleteItem(effect.item)
                }
            }

            is UIEffect.ToggleItem -> {
                scope.launch {
                    itemActionHandler.updateItemChecked(
                        itemId = effect.itemId,
                        newChecked = effect.newChecked
                    )
                }
            }

            is UIEffect.LoadUserProfile -> {
                scope.launch {
                    viewModel.performLoadUserProfile(effect)
                }
            }

            is UIEffect.UpdateUserProfile -> {
                scope.launch {
                    viewModel.performUpdateUserProfile(effect)
                }
            }

            is UIEffect.DeleteAccount -> {
                scope.launch {

                    val userId = authProvider.getCurrentUserUidOrNull() ?: return@launch

                    viewModel.performDeleteAccountFlow(
                        userId = userId,
                        getIdToken = { null }
                    )
                }
            }

            is UIEffect.UnlinkGoogle -> {
                scope.launch {
                    viewModel.performUnlinkGoogle()
                }
            }

            else -> {
                Log.w("UI_EFFECT", "Unhandled effect: $effect")
            }
        }
    }
}