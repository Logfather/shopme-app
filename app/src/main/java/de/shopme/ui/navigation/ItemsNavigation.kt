package de.shopme.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import de.shopme.presentation.screens.ItemsScreen
import de.shopme.presentation.viewmodel.ShoppingViewModel

const val ITEMS_ROUTE = "items/{listId}"

fun NavGraphBuilder.itemsScreen(
    vm: ShoppingViewModel
) {

    composable(ITEMS_ROUTE) { backStackEntry ->

        val listId =
            backStackEntry.arguments?.getString("listId")

        ItemsScreen(
            vm = vm,
            listId = listId
        )
    }
}