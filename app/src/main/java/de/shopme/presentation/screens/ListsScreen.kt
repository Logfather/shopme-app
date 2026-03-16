package de.shopme.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.shopme.presentation.viewmodel.ShoppingViewModel

@Composable
fun ListsScreen(
    vm: ShoppingViewModel,
    onListSelected: (String) -> Unit
) {

    val state by vm.state.collectAsStateWithLifecycle()
    val lists = state.lists

    LazyColumn {

        items(lists) { list ->

            ListItem(
                headlineContent = { Text(list.name) },
                modifier = Modifier.clickable {
                    onListSelected(list.id)
                }
            )
        }
    }
}