package de.shopme.presentation.state

import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.model.ShoppingList

data class ShoppingState(

    val lists: List<ShoppingList> = emptyList(),

    val activeListId: String? = null,

    val items: List<ShoppingItem> = emptyList(),

    val screenMode: ShoppingScreenMode = ShoppingScreenMode.Loading,

    val isRecording: Boolean = false,

    val isLoading: Boolean = false,

    val error: String? = null,

    val isJoining: Boolean = false,

    val inviteListIds: List<String> = emptyList(),   // ✅ EINZIGER SOURCE OF TRUTH

    val inviteSenderName: String? = null,

    val showProfileScreen: Boolean = false,

    val profileTriggeredByShare: Boolean = false,

    val isDataLoaded: Boolean = false,

    val showInviteDialog: Boolean = false
)