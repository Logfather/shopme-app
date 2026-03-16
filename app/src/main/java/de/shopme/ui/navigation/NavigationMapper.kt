package de.shopme.ui.navigation

import de.shopme.presentation.navigation.Screen
import de.shopme.presentation.state.ShoppingScreenMode

fun ShoppingScreenMode.toScreen(): Screen {
    return when (this) {

        ShoppingScreenMode.Loading ->
            Screen.Loading

        ShoppingScreenMode.MultiOverview ->
            Screen.ListsOverview

        ShoppingScreenMode.Normal ->
            Screen.Items

        is ShoppingScreenMode.MultiSelect ->
            Screen.StoreSelection
    }
}