package de.shopme.presentation.shopping.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.ui.theme.BrandGreen

@Composable
fun MultiOverviewScreen(
    lists: List<ShoppingListEntity>,
    activeListId: String?,
    onEdit: (ShoppingListEntity) -> Unit,
    onDelete: (ShoppingListEntity) -> Unit,
    onCreateNewList: () -> Unit
) {

    if (lists.isEmpty()) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Du hast noch keine Einkaufsliste",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onCreateNewList
                ) {
                    Text("Liste erstellen")
                }
            }
        }

        return
    }

    val customLists =
        lists.filter { it.isCustom }
            .sortedBy { it.name }

    val storeLists =
        lists.filter { !it.isCustom }
            .sortedBy { it.name }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        // =========================
        // CUSTOM LISTS
        // =========================

        if (customLists.isNotEmpty()) {

            item {
                Surface(
                    color = BrandGreen,
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = "Individuelle Listen",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            items(customLists) { list ->

                ListRow(
                    list = list,
                    activeListId = activeListId,
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Divider()
                Spacer(Modifier.height(8.dp))
            }
        }

        // =========================
        // STORE LISTS
        // =========================

        if (storeLists.isNotEmpty()) {

            item {
                Surface(
                    color = BrandGreen,
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = "Supermärkte",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            items(storeLists) { list ->

                ListRow(
                    list = list,
                    activeListId = activeListId,
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
private fun ListRow(
    list: ShoppingListEntity,
    activeListId: String?,
    onEdit: (ShoppingListEntity) -> Unit,
    onDelete: (ShoppingListEntity) -> Unit
) {

    val isActive = list.id == activeListId

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(list) },
        tonalElevation = if (isActive) 6.dp else 1.dp,
        shape = MaterialTheme.shapes.medium
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val icon =
                list.storeTypes.firstOrNull()?.logoRes
                    ?: R.drawable.store_icon76

            Image(
                painter = painterResource(id = icon),
                contentDescription = list.name,
                modifier = Modifier.size(36.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = list.name,
                    style = MaterialTheme.typography.titleMedium
                )

                if (isActive) {
                    Text(
                        text = "Aktive Liste",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            IconButton(
                onClick = { onEdit(list) }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Bearbeiten"
                )
            }

            IconButton(
                onClick = { onDelete(list) }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Löschen"
                )
            }
        }
    }
}