package de.shopme.presentation.state

import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.model.ShoppingList
import de.shopme.domain.model.ShoppingListEntity

data class ShoppingState(
    val lists: List<ShoppingList> = emptyList(),
    val activeListId: String? = null,
    val items: List<ShoppingItem> = emptyList(),
    val screenMode: ShoppingScreenMode = ShoppingScreenMode.Loading,
    val isRecording: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isJoining: Boolean = false,
    val inviteListIds: List<String> = emptyList(),
    val inviteSenderName: String? = null,
    val showProfileScreen: Boolean = false,
    val profileTriggeredByShare: Boolean = false,
    val isDataLoaded: Boolean = false,
    val showInviteDialog: Boolean = false,
    val isInviteLoading: Boolean = false,
    val inviteError: String? = null,
    val inviteResolvedLists: List<ShoppingList> = emptyList(),
    val currentInviteId: String? = null,
    val inviteId: String? = null,
    val showDeleteAllConfirm: Boolean = false,
    val isDeletingAll: Boolean = false,
    val deleteGeneration: Int = 0,
    val isSorting: Boolean = false,
    val sortingPhase: SortingPhase = SortingPhase.Idle,
    val isSharing: Boolean = false,
    val displayName: String? = null,
    val hasProfile: Boolean = false,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val showSaveChoice: Boolean = false
)

enum class SortingPhase {
    Idle,
    Preparing,
    Sorting,
    Finalizing
}