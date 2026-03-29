package de.shopme.presentation.reducer

import de.shopme.domain.model.SyncStatus
import de.shopme.presentation.action.ShoppingAction
import de.shopme.presentation.event.ShopEvent
import de.shopme.presentation.state.ShoppingScreenMode
import de.shopme.presentation.state.ShoppingState
import de.shopme.presentation.effect.UIEffect
import de.shopme.presentation.undo.UndoAction

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

                effects = listOf(
                    UIEffect.AddItem(it.name)   // ✅ Effect setzen
                )

                state   // ✅ State bleibt gleich
            }

            is ShopEvent.Item.Toggle -> {

                effects = listOf(
                    UIEffect.ToggleItem(it.item),

                    UIEffect.ShowUndo(
                        action = de.shopme.presentation.undo.UndoAction.ToggleItem(it.item),
                        message = "Status geändert"
                    )
                )

                state
            }

            is ShopEvent.Item.RetrySync -> {

                effects = listOf(
                    UIEffect.RetrySync(it.itemId)
                )

                state
            }

            is ShopEvent.Item.Delete -> {

                effects = listOf(
                    UIEffect.DeleteItem(it.item),

                    UIEffect.ShowUndo(
                        action = UndoAction.DeleteItem(it.item),
                        message = "Item gelöscht"
                    )
                )

                state
            }

            is ShopEvent.Item.Update -> {

                effects = listOf(
                    UIEffect.UpdateItem(it.item, it.newName),

                    UIEffect.ShowUndo(
                        action = UndoAction.UpdateItem(it.item),
                        message = "Item geändert"
                    )
                )

                state.copy(
                    items = state.items.map { item ->
                        if (item.id == it.item.id) {
                            item.copy(
                                name = it.newName,
                                isChecked = true,
                                updatedAt = System.currentTimeMillis(),
                                syncStatus = SyncStatus.Pending
                            )
                        } else item
                    }
                )
            }

            is ShopEvent.List.DeleteAllLists -> {

                effects = listOf(
                    UIEffect.DeleteAllLists
                )

                state
            }

            is ShopEvent.List.Delete -> {

                effects = listOf(
                    UIEffect.DeleteList(it.listId)
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