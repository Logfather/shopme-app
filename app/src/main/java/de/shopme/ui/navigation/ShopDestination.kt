package de.shopme.presentation.navigation

sealed class ShopDestination {

    object Overview : ShopDestination()

    object Items : ShopDestination()

    object StoreSelection : ShopDestination()

}