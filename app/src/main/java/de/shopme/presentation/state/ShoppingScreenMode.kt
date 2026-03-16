package de.shopme.presentation.state

import de.shopme.presentation.navigation.Screen
import de.shopme.domain.model.StoreType

sealed class ShoppingScreenMode {

    object Loading : ShoppingScreenMode()

    object MultiOverview : ShoppingScreenMode()

    data class MultiSelect(
        val selectedStores: List<StoreType>
    ) : ShoppingScreenMode()

    object Normal : ShoppingScreenMode()

}