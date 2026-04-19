package de.shopme.presentation.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.shopme.domain.model.ShoppingList
import de.shopme.presentation.event.ShopEvent
import de.shopme.presentation.viewmodel.ShoppingViewModel

// ===================== SCREEN =====================

//@Composable
//fun ListsScreen(
//    vm: ShoppingViewModel,
//    onListSelected: (String) -> Unit
//) {
//
//    val state by vm.state.collectAsStateWithLifecycle()
//    val lists = state.lists
//
//    LazyColumn {
//        items(
//            items = lists,
//            key = { it.id }
//        ) { list ->
//
//            ListRow(
//                list = list,
//                itemCount = state.items.count { item -> item.listId == list.id },
//                onEdit = { selectedList ->
//                    onListSelected(selectedList.id)
//                },
//                onDelete = { deletedList ->
//                    vm.onEvent(ShopEvent.List.Delete(deletedList.id))
//                }
//            )
//        }
//    }
//}
//
//// ===================== ROW =====================
//
//@Composable
//private fun ListRow(
//    list: ShoppingList,
//    itemCount: Int,
//    onEdit: (ShoppingList) -> Unit,
//    onDelete: (ShoppingList) -> Unit
//) {
//
//    Log.d("SWIPE_DEBUG", "ListRow aufgebaut: ${list.id}")
//
//    var removeTrigger by remember(list.id) { mutableStateOf(0) }
//
//    val dismissState = rememberSwipeToDismissBoxState(
//        confirmValueChange = { value ->
//            Log.d("SWIPE_DEBUG", "State Change: $value") // 🔥 HIER
//
//            if (value == SwipeToDismissBoxValue.EndToStart) {
//                removeTrigger++
//                true
//            } else {
//                false
//            }
//        }
//    )
//
//    // 👉 korrektes Warten auf Animation
//    LaunchedEffect(removeTrigger) {
//        Log.d("SWIPE_DEBUG", "removeTrigger: $removeTrigger")
//        if (removeTrigger > 0) {
//            while (dismissState.currentValue != SwipeToDismissBoxValue.EndToStart) {
//                kotlinx.coroutines.delay(16)
//            }
//            onDelete(list)
//        }
//    }
//
//    SwipeToDismissBox(
//        state = dismissState,
//        enableDismissFromStartToEnd = false,
//        backgroundContent = {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(MaterialTheme.colorScheme.errorContainer)
//                    .padding(horizontal = 20.dp),
//                contentAlignment = Alignment.CenterEnd
//            ) {
//                Text(
//                    text = "Löschen",
//                    color = MaterialTheme.colorScheme.onErrorContainer
//                )
//            }
//        }
//    ) {
//        ListItem(
//            headlineContent = { Text(list.name) },
//            supportingContent = { Text("$itemCount Artikel") },
//            modifier = Modifier.clickable {
//                onEdit(list)
//            }
//        )
//    }
//
//    LaunchedEffect(dismissState.currentValue) {
//        Log.d("SWIPE_DEBUG", "currentValue: ${dismissState.currentValue}")
//    }
//
//    LaunchedEffect(dismissState.targetValue) {
//        Log.d("SWIPE_DEBUG", "targetValue: ${dismissState.targetValue}")
//    }
//}