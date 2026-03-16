package de.shopme.presentation.reducer

import de.shopme.presentation.action.ShoppingAction
import de.shopme.presentation.event.ShopEvent
import de.shopme.presentation.state.ShoppingScreenMode
import de.shopme.presentation.state.ShoppingState
import de.shopme.presentation.effect.UIEffect

data class ReducerResult(
    val state: ShoppingState,
    val effects: List<UIEffect> = emptyList()
)

fun reduce(
    state: ShoppingState,
    screenMode: ShoppingScreenMode,
    event: ShopEvent? = null,
    action: ShoppingAction? = null
): ReducerResult {

    var newState = state
    var effects: List<UIEffect> = emptyList()

    // ------------------------------------------------------------
    // EVENTS (Item / List mutations)
    // ------------------------------------------------------------

    event?.let {

        newState = when (it) {

            is ShopEvent.Item.Add -> {
                state
            }

            is ShopEvent.Item.Toggle -> {

                val updatedItems = state.items.map { item ->
                    if (item.id == it.item.id)
                        item.copy(isChecked = !item.isChecked)
                    else
                        item
                }

                state.copy(items = updatedItems)
            }

            is ShopEvent.Item.Delete -> {

                state.copy(
                    items = state.items.filter { item ->
                        item.id != it.item.id
                    }
                )
            }

            is ShopEvent.Item.SetChecked -> {

                val updatedItems = state.items.map { item ->
                    if (item.id == it.item.id)
                        item.copy(isChecked = it.checked)
                    else
                        item
                }

                state.copy(items = updatedItems)
            }

            ShopEvent.List.ClearAll -> {

                state.copy(items = emptyList())
            }

            else -> state
        }
    }

    // ------------------------------------------------------------
    // ACTIONS (UI navigation / flows)
    // ------------------------------------------------------------

    action?.let {

        when (state.screenMode) {

            ShoppingScreenMode.Loading -> when (it) {

                ShoppingAction.StartMultiStoreCreation -> {
                    newState = newState.copy(
                        screenMode = ShoppingScreenMode.MultiSelect(emptyList())
                    )
                }

                else -> Unit
            }

            ShoppingScreenMode.Normal -> when (it) {

                ShoppingAction.StartMultiStoreCreation -> {
                    newState = newState.copy(
                        screenMode = ShoppingScreenMode.MultiSelect(emptyList())
                    )
                }

                ShoppingAction.CancelMultiCreation -> {
                    newState = newState.copy(
                        screenMode = ShoppingScreenMode.MultiOverview
                    )
                }

                else -> Unit
            }

            ShoppingScreenMode.MultiOverview -> when (it) {

                ShoppingAction.StartMultiStoreCreation -> {
                    newState = newState.copy(
                        screenMode = ShoppingScreenMode.MultiSelect(emptyList())
                    )
                }

                else -> Unit
            }

            is ShoppingScreenMode.MultiSelect -> when (it) {

                is ShoppingAction.ToggleStore -> {

                    val current = state.screenMode as ShoppingScreenMode.MultiSelect

                    val updated =
                        if (it.store in current.selectedStores)
                            current.selectedStores - it.store
                        else
                            current.selectedStores + it.store

                    newState = newState.copy(
                        screenMode = ShoppingScreenMode.MultiSelect(updated)
                    )
                }

                is ShoppingAction.ConfirmStores -> {

                    val stores =
                        (screenMode as? ShoppingScreenMode.MultiSelect)
                            ?.selectedStores
                            ?: emptyList()

                    effects = listOf(
                        UIEffect.CreateLists(
                            stores = stores,
                            customLists = it.customLists
                        )
                    )

                    newState = newState.copy(
                        screenMode = ShoppingScreenMode.MultiOverview
                    )
                }

                ShoppingAction.CancelMultiCreation -> {

                    newState = newState.copy(
                        screenMode = ShoppingScreenMode.MultiOverview
                    )
                }

                else -> Unit
            }
        }
    }

    return ReducerResult(
        state = newState,
        effects = effects
    )
}