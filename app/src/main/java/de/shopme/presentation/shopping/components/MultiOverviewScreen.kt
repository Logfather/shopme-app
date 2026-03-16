package de.shopme.presentation.shopping.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import de.shopme.domain.model.ShoppingList
import de.shopme.ui.illustration.icons.shopicons.StoreIcon
import de.shopme.ui.theme.BrandGreen
import de.shopme.ui.theme.BrandOlive
import de.shopme.presentation.state.ShoppingViewState

@Composable
fun MultiOverviewScreen(
    viewState: ShoppingViewState,
    lists: List<ShoppingList>,
    activeListId: String?,
    onEdit: (ShoppingList) -> Unit,
    onDelete: (ShoppingList) -> Unit,
    onCreateNewList: () -> Unit
) {

    val customLists =
        lists.filter { it.storeTypes.isEmpty() }

    val storeLists =
        lists.filter { it.storeTypes.isNotEmpty() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 6.dp)
    ) {

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
                    viewState = viewState,
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

        if (storeLists.isNotEmpty()) {

            item {
                Surface(
                    color = BrandOlive,
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
                    viewState = viewState,
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
    viewState: ShoppingViewState,
    list: ShoppingList,
    activeListId: String?,
    onEdit: (ShoppingList) -> Unit,
    onDelete: (ShoppingList) -> Unit
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
            tonalElevation = if (isActive) 6.dp else 1.dp
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
                            BrandGreen
                        else
                            BrandOlive
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

                        StoreIcon(
                            store = store,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(Modifier.width(12.dp))
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = buildAnnotatedString {

                                append(list.name)

                                append("    ")

                                withStyle(
                                    style = SpanStyle(
                                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                        color = Color.Black
                                    )
                                ) {
                                    append("(${list.itemCount} Artikel)")
                                }
                            },
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text =
                                if (isActive)
                                    "Aktive Liste"
                                else
                                    "Tippen zum Öffnen",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}