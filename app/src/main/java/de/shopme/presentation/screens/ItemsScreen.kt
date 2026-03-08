package de.shopme.presentation.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.domain.model.ShoppingItem

@Composable
fun ItemsScreen(
    vm: ShoppingViewModel,
    listId: String?
) {

    val groupedItems by vm.groupedItems.collectAsStateWithLifecycle()

    val items: List<ShoppingItem> =
        groupedItems.values.flatten()

    LazyColumn {

        items(items) { item ->

            Text(item.name)

        }
    }
}