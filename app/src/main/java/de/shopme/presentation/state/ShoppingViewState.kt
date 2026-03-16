package de.shopme.presentation.state

import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.model.ShoppingList

data class ShoppingViewState(

    val uiState: ShoppingScreenMode = ShoppingScreenMode.Loading,

    val lists: List<ShoppingList> = emptyList(),

    val activeList: ShoppingList? = null,

    val groupedItems: Map<String, List<ShoppingItem>> = emptyMap(),

    val snackbarMessage: String? = null,

    val showWelcomeDialog: Boolean = false,

    val showStoreSelectionDialog: Boolean = false
)