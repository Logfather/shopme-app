package de.shopme.presentation.navigation

sealed class Screen {

    object Loading : Screen()

    object ListsOverview : Screen()

    object Items : Screen()

    object StoreSelection : Screen()

}