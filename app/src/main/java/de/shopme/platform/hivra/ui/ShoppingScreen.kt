package de.shopme.platform.hivra.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import de.shopme.R
import de.shopme.domain.model.ShoppingItem
import de.shopme.platform.nimblu.state.EmptyState
import de.shopme.ui.components.SupermarketItemRow


@Composable
fun ShoppingScreen(
    items: List<ShoppingItem>,
    onAddItem: (String) -> Unit,
    onToggleItem: (ShoppingItem) -> Unit
) {

    var input by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { ShoppingTopBar() },
        bottomBar = {
            AddItemBar(
                value = input,
                onValueChange = { input = it },
                onAdd = {
                    if (input.isNotBlank()) {
                        onAddItem(input)
                        input = ""
                    }
                }
            )
        }
    ) { padding ->

        if (items.isEmpty()) {

            EmptyState(
                title = "Noch nichts auf deiner Liste",
                subtitle = "Füge dein erstes Item hinzu",
                icon = painterResource(R.drawable.nimblu_empty_bag),
                action = { /* optional */ }
            )

        } else {

            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {

                items(
                    items = items,
                    key = { it.id }
                ) { item ->

                    SupermarketItemRow(
                        item = item,
                        categoryColor = Color.Gray,

                        onToggle = {
                            onToggleItem(item)
                        },

                        onDelete = {
                            // TODO später sauber integrieren
                        },

                        onRetry = {
                            // TODO später sauber integrieren
                        },

                        onUpdate = {
                            // TODO später sauber integrieren
                        }
                    )
                }
            }
        }
    }
}