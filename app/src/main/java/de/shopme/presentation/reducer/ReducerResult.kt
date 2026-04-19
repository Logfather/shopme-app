package de.shopme.presentation.reducer

import android.util.Log
import de.shopme.domain.model.SyncStatus
import de.shopme.presentation.action.ShoppingAction
import de.shopme.presentation.event.ShopEvent
import de.shopme.presentation.state.ShoppingScreenMode
import de.shopme.presentation.state.ShoppingState
import de.shopme.presentation.effect.UIEffect
import de.shopme.presentation.state.SortingPhase


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
    // EVENTS
    // ------------------------------------------------------------

    event?.let {

        newState = when (it) {

            is ShopEvent.Item.Add -> {
                effects = listOf(UIEffect.AddItem(it.name))
                state
            }

            is ShopEvent.Item.Toggle -> {

                val newChecked = !it.item.isChecked

                effects = listOf(
                    UIEffect.ToggleItem(
                        itemId = it.item.id,
                        newChecked = newChecked
                    )
                )

                state
            }

            is ShopEvent.Item.RetrySync -> {
                effects = listOf(UIEffect.RetrySync(it.itemId))
                state
            }

            is ShopEvent.Item.Delete -> {
                effects = listOf(
                    UIEffect.DeleteItem(it.item),
                    UIEffect.ShowUndo("Item gelöscht")
                )
                state
            }

            is ShopEvent.Item.Update -> {

                val updatedItem = it.item.copy(
                    name = it.newName,
                    isChecked = true,
                    updatedAt = System.currentTimeMillis()
                )

                effects = listOf(
                    UIEffect.UpdateItem(
                        item = updatedItem,
                        newName = it.newName
                    ),
                    UIEffect.ShowUndo("Item geändert (Update)")
                )

                state // ✅ das ist der Return für den when-Block
            }

            is ShopEvent.List.DeleteAllLists -> {
                effects = listOf(UIEffect.DeleteAllLists)
                state
            }

            is ShopEvent.List.Delete -> {
                effects = listOf(UIEffect.RequestDeleteList(it.listId))
                state
            }

            is ShopEvent.List.StartSorting -> {
                state.copy(isSorting = true, sortingPhase = SortingPhase.Preparing)
            }

            is ShopEvent.List.SetSortingPhase -> {
                state.copy(sortingPhase = it.phase)
            }

            is ShopEvent.List.FinishSorting -> {
                state.copy(isSorting = false, sortingPhase = SortingPhase.Idle)
            }

            is ShopEvent.List.StartDeleteAll -> {
                state.copy(isDeletingAll = true)
            }

            is ShopEvent.List.FinishDeleteAll -> {
                state.copy(isDeletingAll = false)
            }

            is ShopEvent.List.StartSharing -> {
                Log.d("SHARE_FLOW", "Reducer → StartSharing")
                state.copy(isSharing = true)
            }

            is ShopEvent.List.FinishSharing -> {
                state.copy(isSharing = false)
            }

            is ShopEvent.System.OpenProfileScreen -> {
                state.copy(showProfileScreen = false)
            }

            is ShopEvent.System.ShowSaveChoice -> {
                state.copy(showSaveChoice = true)
            }

            is ShopEvent.System.HideSaveChoice -> {
                state.copy(showSaveChoice = false)
            }

            is ShopEvent.System.ConfirmGoogleSave -> {
                effects = effects + UIEffect.StartGoogleSignIn
                state.copy(showSaveChoice = false)
            }

            is ShopEvent.System.ConfirmManualSave -> {
                effects = effects + UIEffect.UpdateUserProfile(
                    uid = "",
                    nickName = it.nickName,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    email = it.email
                )
                state.copy(showSaveChoice = false)
            }

            else -> state
        }
    }

    // ------------------------------------------------------------
    // ACTIONS
    // ------------------------------------------------------------

    action?.let {

        when (it) {

            is ShoppingAction.ToggleItem -> {
                val newChecked = !it.item.isChecked

                effects = listOf(
                    UIEffect.ToggleItem(
                        itemId = it.item.id,
                        newChecked = newChecked
                    )
                )
            }

            is ShoppingAction.UpdateUserProfile -> {
                effects = effects + UIEffect.UpdateUserProfile(
                    uid = it.uid,
                    nickName = it.nickName,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    email = it.email
                )

                newState = newState.copy(
                    displayName = it.nickName,
                    hasProfile = true,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    email = it.email
                )
            }

            is ShoppingAction.LoadUserProfile -> {
                effects = effects + UIEffect.LoadUserProfile(it.uid)
            }

            is ShoppingAction.UserProfileLoaded -> {
                val profileName = it.profileName?.trim()
                val hasValidProfile = !profileName.isNullOrBlank()

                newState = newState.copy(
                    displayName = profileName,
                    hasProfile = hasValidProfile,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    email = it.email
                )
            }

            else -> Unit
        }

        // ------------------------------------------------------------
        // SCREEN MODE HANDLING
        // ------------------------------------------------------------

        when (screenMode) {

            ShoppingScreenMode.Loading,
            ShoppingScreenMode.Normal,
            ShoppingScreenMode.MultiOverview -> {

                when (it) {

                    ShoppingAction.StartMultiStoreCreation -> {
                        newState = newState.copy(
                            screenMode = ShoppingScreenMode.MultiSelect(emptyList())
                        )
                    }

                    is ShoppingAction.DeleteAllLists -> {
                        effects = listOf(UIEffect.DeleteAllLists)
                    }

                    else -> Unit
                }
            }

            is ShoppingScreenMode.MultiSelect -> {

                when (it) {

                    is ShoppingAction.ToggleStore -> {

                        val current = screenMode

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

                        val stores = screenMode.selectedStores

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

                    is ShoppingAction.DeleteAllLists -> {
                        effects = listOf(UIEffect.DeleteAllLists)
                    }

                    else -> Unit
                }
            }
        }
    }

    return ReducerResult(
        state = newState,
        effects = effects
    )
}