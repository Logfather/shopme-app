package de.shopme.presentation.shopping

sealed class ShoppingUiState {

    object Loading : ShoppingUiState()

    object Normal : ShoppingUiState()

    object MultiSelect : ShoppingUiState()

    object MultiOverview : ShoppingUiState()
}