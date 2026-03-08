package de.shopme.presentation.shopping

import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.model.ShoppingListEntity

data class ShoppingViewState(

    val uiState: ShoppingUiState = ShoppingUiState.Loading,

    val lists: List<ShoppingListEntity> = emptyList(),

    val activeList: ShoppingListEntity? = null,

    val groupedItems: Map<String, List<ShoppingItem>> = emptyMap(),

    val snackbarMessage: String? = null,

    val showWelcomeDialog: Boolean = false,

    val showStoreSelectionDialog: Boolean = false
)