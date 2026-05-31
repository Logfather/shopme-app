package de.shopme.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.shopme.domain.model.ShoppingList
import de.shopme.presentation.event.ShopEvent
import de.shopme.presentation.viewmodel.ShoppingViewModel

// ===================== SCREEN =====================

@Composable
fun ListsScreen(
    vm: ShoppingViewModel,
    onListSelected: (String) -> Unit
) {

    val state by vm.state.collectAsStateWithLifecycle()
    val lists = state.lists

    LazyColumn {
        items(
            items = lists,
            key = { it.id }
        ) { list ->

            ListRow(
                list = list,
                itemCount = state.items.count { item -> item.listId == list.id },
                onEdit = { selectedList ->
                    onListSelected(selectedList.id)
                },
                onDelete = { deletedList ->
                    vm.onEvent(ShopEvent.List.Delete(deletedList.id))
                }
            )
        }
    }
}

// ===================== ROW =====================

@Composable
private fun ListRow(
    list: ShoppingList,
    itemCount: Int,
    onEdit: (ShoppingList) -> Unit,
    onDelete: (ShoppingList) -> Unit
) {

    var removeTrigger by remember(list.id) { mutableStateOf(0) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->

            if (value == SwipeToDismissBoxValue.EndToStart) {
                removeTrigger++
                true
            } else {
                false
            }
        }
    )

    // 👉 korrektes Warten auf Animation
    LaunchedEffect(removeTrigger) {
        if (removeTrigger > 0) {
            while (dismissState.currentValue != SwipeToDismissBoxValue.EndToStart) {
                kotlinx.coroutines.delay(16)
            }
            onDelete(list)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Löschen",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        ListItem(
            headlineContent = { Text(list.name) },
            supportingContent = { Text("$itemCount Artikel") },
            modifier = Modifier.clickable {
                onEdit(list)
            }
        )
    }
}