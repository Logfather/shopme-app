package de.shopme.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import de.shopme.presentation.viewmodel.ShoppingViewModel

//@Composable
//fun ShopMeNavGraph(
//    vm: ShoppingViewModel
//) {
//
//    val navController = rememberNavController()
//
//    NavHost(
//        navController = navController,
//        startDestination = LISTS_ROUTE
//    ) {
//
//        listsScreen(
//            vm = vm,
//            navController = navController
//        )
//
//        itemsScreen(
//            vm = vm
//        )
//    }
//}