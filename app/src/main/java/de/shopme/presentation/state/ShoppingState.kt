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

    val error: String? = null

)