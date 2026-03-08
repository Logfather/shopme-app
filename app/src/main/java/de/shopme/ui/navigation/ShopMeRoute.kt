package de.shopme.ui.navigation

sealed class ShopMeRoute(val route: String) {

    object Lists : ShopMeRoute("lists")

    object Items : ShopMeRoute("items/{listId}") {

        fun create(listId: String): String {
            return "items/$listId"
        }
    }
}