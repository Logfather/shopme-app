package de.shopme.presentation.shopping

import de.shopme.presentation.navigation.Screen
import de.shopme.domain.model.StoreType

sealed class ShoppingUiState {

    object Loading : ShoppingUiState()

    object MultiOverview : ShoppingUiState()

    data class MultiSelect(
        val selectedStores: List<StoreType>
    ) : ShoppingUiState()

    object Normal : ShoppingUiState()
}

fun ShoppingUiState.toScreen(): Screen {

    return when (this) {

        ShoppingUiState.Loading ->
            Screen.Loading

        ShoppingUiState.MultiOverview ->
            Screen.ListsOverview

        ShoppingUiState.Normal ->
            Screen.Items

        is ShoppingUiState.MultiSelect ->
            Screen.StoreSelection

    }
}