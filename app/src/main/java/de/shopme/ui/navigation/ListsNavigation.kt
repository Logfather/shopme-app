package de.shopme.ui.navigation
//
//import androidx.navigation.NavGraphBuilder
//import androidx.navigation.compose.composable
//import androidx.navigation.NavController
//import de.shopme.presentation.screens.ListsScreen
//import de.shopme.presentation.viewmodel.ShoppingViewModel
//
//const val LISTS_ROUTE = "lists"
//
//fun NavGraphBuilder.listsScreen(
//    vm: ShoppingViewModel,
//    navController: NavController
//) {
//
//    composable(LISTS_ROUTE) {
//
//        ListsScreen(
//            vm = vm,
//            onListSelected = { listId ->
//                navController.navigate("items/$listId")
//            }
//        )
//    }
//}