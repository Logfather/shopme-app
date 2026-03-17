package de.shopme.presentation.reducer

import android.util.Log
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

                Log.d("EVENT_DEBUG", "Reducer → AddItem: ${it.name}")

                effects = listOf(
                    UIEffect.AddItem(it.name)   // ✅ Effect setzen
                )

                state   // ✅ State bleibt gleich
            }

            is ShopEvent.Item.Toggle -> {

                effects = listOf(
                    UIEffect.ToggleItem(it.item)
                )

                state
            }

            is ShopEvent.Item.Delete -> {

                effects = listOf(
                    UIEffect.DeleteItem(it.item)
                )

                state
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