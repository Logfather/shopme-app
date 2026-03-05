package de.shopme.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import de.shopme.presentation.screens.StateScreen
import de.shopme.presentation.screens.ListsScreen
import de.shopme.presentation.screens.ItemsScreen

@Composable
fun ShopMeNavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "state"
    ) {

        composable("state") {
            StateScreen()
        }

        composable("lists") {
            ListsScreen()
        }

        composable("items/{listId}") {
            ItemsScreen()
        }

    }
}