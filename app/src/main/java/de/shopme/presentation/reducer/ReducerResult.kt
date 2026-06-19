package de.shopme.presentation.reducer

import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.presentation.action.ShoppingAction
import de.shopme.presentation.effect.UIEffect
import de.shopme.presentation.event.ShopEvent
import de.shopme.presentation.state.ShoppingScreenMode
import de.shopme.presentation.state.ShoppingState
import de.shopme.presentation.state.SortingPhase
import de.shopme.presentation.state.deduplicate


data class ReducerResult(
    val state: ShoppingState,
    val effects: List<UIEffect> = emptyList()
)

fun reduce(
    state: ShoppingState,
    action: ShoppingAction? = null,
    event: ShopEvent? = null
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

            is ShopEvent.Speech.AddItemFromSpeech -> {

                RuntimeLog.reducer(
                    "Speech event received: ${it.text}"
                )

                effects = listOf(
                    UIEffect.ProcessSpeech(it.text)
                )

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
                    UIEffect.DeleteItem(it.item)
                )
                state
            }

            is ShopEvent.Item.Update -> {

                val updatedItem = it.item.copy(
                    name = it.newName,
                    updatedAt = System.currentTimeMillis()
                )

                effects = listOf(
                    UIEffect.UpdateItem(
                        item = updatedItem,
                        newName = it.newName
                    )
                )

                state
            }

            is ShopEvent.List.DeleteAllLists -> {
                effects = listOf(UIEffect.DeleteAllLists)
                state
            }

            is ShopEvent.List.Delete -> {

                effects = listOf(
                    UIEffect.RequestDeleteList(it.listId)
                )

                val updatedLists = state.lists.filterNot { list ->
                    list.id == it.listId
                }


                state.copy(
                    lists = updatedLists,
                    activeListId =
                        if (state.activeListId == it.listId) {
                            null
                        } else {
                            state.activeListId
                        }
                )
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

                val hasProfile = state.hasProfile

                val updatedState = state.copy(
                    isSharing = true,
                    profileTriggeredByShare = !hasProfile,
                    pendingShareListId = it.listId
                )

                if (hasProfile) {
                    effects = effects + UIEffect.ShareList(it.listId)
                }
                updatedState
            }

            is ShopEvent.List.FinishSharing -> {
                state.copy(
                    isSharing = false,
                    showShareSuccess = false
                )
            }

            is ShopEvent.List.ShareStarted -> {
                state.copy(
                    isSharing = false,
                    showShareSuccess = true
                )
            }

            is ShopEvent.System.OpenProfileScreen -> {
                state.copy(showProfileScreen = true)
            }

            is ShopEvent.System.ShowSaveChoice -> {
                state.copy(showSaveChoice = true)
            }

            is ShopEvent.System.HideSaveChoice -> {
                state.copy(showSaveChoice = false)
            }

            is ShopEvent.System.ConfirmGoogleSave -> {

                effects = effects + UIEffect.StartGoogleSignIn

                state.copy(
                    showSaveChoice = false,
                    showProfileScreen = false
                )
            }

            is ShopEvent.System.ConfirmManualSave -> {

                effects = effects + UIEffect.UpdateUserProfile(
                    uid = "",
                    nickName = it.nickName,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    email = it.email
                )

                // 🔥 WICHTIG: Share Flow fortsetzen
                val shareListId = newState.pendingShareListId

                if (state.profileTriggeredByShare && shareListId != null) {

                    effects = effects + UIEffect.ShareList(shareListId)
                }

                state.copy(
                    showSaveChoice = false,
                    showProfileScreen = false, // 🔥 FIX
                    profileTriggeredByShare = false
                )
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

            is ShoppingAction.FinishListCreation -> {

                newState = newState.copy(
                    screenMode = ShoppingScreenMode.MultiOverview
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

                // 🔥 FALLBACK: Google liefert oft keinen profileName
                val profileName = it.profileName?.trim()
                    ?: it.email?.substringBefore("@")

                val hasValidProfile =
                    !profileName.isNullOrBlank() || it.exists

                val shareListId = newState.pendingShareListId

                if (state.profileTriggeredByShare && shareListId != null) {

                    effects = effects + UIEffect.ShareList(shareListId)

                }

                newState = state.copy(
                    displayName = profileName,
                    hasProfile = hasValidProfile,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    email = it.email,
                    profileTriggeredByShare = false
                )
            }

            else -> Unit
        }

        // ------------------------------------------------------------
        // SCREEN MODE HANDLING
        // ------------------------------------------------------------

        when (state.screenMode) {

            ShoppingScreenMode.Loading,
            ShoppingScreenMode.Normal,
            ShoppingScreenMode.MultiOverview -> {


                when (it) {

                    ShoppingAction.StartMultiStoreCreation -> {
                        newState = newState.copy(
                            screenMode = ShoppingScreenMode.MultiSelect(emptyList())
                        )
                    }

                    else -> Unit
                }
            }

            is ShoppingScreenMode.MultiSelect -> {

                when (it) {

                    is ShoppingAction.ConfirmStores -> {

                        val stores = state.screenMode.selectedStores

                        effects = effects + UIEffect.CreateLists(
                            stores = stores,
                            customLists = it.customLists
                        )

                        newState = newState.copy(
                            screenMode = ShoppingScreenMode.MultiOverview
                        )
                    }

                    is ShoppingAction.ToggleStore -> {

                        val current = state.screenMode

                        val updated =
                            if (it.store in current.selectedStores)
                                current.selectedStores - it.store
                            else
                                current.selectedStores + it.store

                        newState = newState.copy(
                            screenMode = ShoppingScreenMode.MultiSelect(updated)
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
    }

    val safeState = newState.deduplicate()

    return ReducerResult(
        state = safeState,
        effects = effects
    )
}