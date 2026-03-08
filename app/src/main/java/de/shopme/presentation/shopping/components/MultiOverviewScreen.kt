package de.shopme.presentation.shopping.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import de.shopme.ui.theme.BrandGreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.shopme.domain.model.ShoppingListEntity

@Composable
fun MultiOverviewScreen(
    lists: List<ShoppingListEntity>,
    activeListId: String?,
    onEdit: (ShoppingListEntity) -> Unit,
    onDelete: (ShoppingListEntity) -> Unit,
    onCreateNewList: () -> Unit
) {

    val customLists =
        lists.filter { it.storeTypes.isEmpty() }

    val storeLists =
        lists.filter { it.storeTypes.isNotEmpty() }

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

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            onDelete(list)
            true
        }
    )

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

        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = if (isActive) 6.dp else 1.dp,
            shape = MaterialTheme.shapes.medium
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isActive) 6.dp else 2.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor =
                        if (isActive)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEdit(list) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // grüner Active Indicator
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(BrandGreen)
                        )

                        Spacer(Modifier.width(10.dp))
                    }

                    list.storeTypes.firstOrNull()?.let { store ->

                        Image(
                            painter = painterResource(id = store.logoRes),
                            contentDescription = store.displayName,
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(Modifier.width(12.dp))
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = list.name,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text =
                                if (isActive)
                                    "Aktive Liste"
                                else
                                    "Tippen zum Öffnen",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}