package de.shopme.presentation.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import de.shopme.domain.model.ShoppingItem
import de.shopme.presentation.viewmodel.ShoppingViewModel

@Composable
fun ItemsScreen(
    vm: ShoppingViewModel,
    listId: String?
) {

    val items by vm
        .itemsForList(listId ?: return)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    LazyColumn {
        items(
            items = items,
            key = { it.id }
        ) { item ->
            Text(item.name)
        }
    }
}