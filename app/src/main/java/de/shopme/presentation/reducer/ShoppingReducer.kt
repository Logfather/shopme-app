package de.shopme.presentation.reducer

import de.shopme.presentation.action.ShoppingAction
import de.shopme.presentation.shopping.ShoppingUiState

fun reduce(
    state: ShoppingUiState,
    action: ShoppingAction
): ShoppingUiState {

    return when (state) {

        ShoppingUiState.Loading -> when (action) {

            ShoppingAction.StartMultiStoreCreation ->
                ShoppingUiState.MultiSelect(emptyList())

            else -> state
        }

        ShoppingUiState.Normal -> when (action) {

            ShoppingAction.StartMultiStoreCreation ->
                ShoppingUiState.MultiSelect(emptyList())

            else -> state
        }

        ShoppingUiState.MultiOverview -> when (action) {

            ShoppingAction.StartMultiStoreCreation ->
                ShoppingUiState.MultiSelect(emptyList())

            else -> state
        }

        is ShoppingUiState.MultiSelect -> when (action) {

            ShoppingAction.CancelMultiCreation ->
                ShoppingUiState.MultiOverview

            else -> state
        }
    }
}