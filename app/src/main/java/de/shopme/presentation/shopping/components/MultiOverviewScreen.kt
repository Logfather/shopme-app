package de.shopme.presentation.shopping.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.shopme.domain.model.ShoppingList
import de.shopme.presentation.screens.DeleteAllListsScreen
import de.shopme.presentation.state.ShoppingViewState
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.ui.illustration.icons.shopicons.StoreIcon
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandGreen
import de.shopme.ui.theme.BrandGrey
import de.shopme.ui.theme.BrandOlive

@Composable
fun MultiOverviewScreen(
    viewModel: ShoppingViewModel,
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

    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        if (state.isDeletingAll){
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)),
                color = BrandGreen,
                trackColor = BrandOlive,
                strokeCap = StrokeCap.Round
            )
        }

        val itemCounts = remember(viewState.groupedItems) {

            viewState.groupedItems
                .values
                .flatten()
                .groupingBy { it.listId }
                .eachCount()
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {

            // ---------------- CUSTOM LISTS ----------------

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

                items<ShoppingList>(
                    items = customLists,
                    key = { it.id }
                ) { list ->

                    val itemCount =
                        itemCounts[list.id] ?: 0

                    ListRow(
                        list = list,
                        activeListId = activeListId,
                        itemCount = itemCount,
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

            // ---------------- STORE LISTS ----------------

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

                items<ShoppingList>(
                    items = storeLists,
                    key = { it.id }
                ){ list ->

                    val itemCount =
                        viewState.groupedItems
                            .values
                            .flatten()
                            .count { it.listId == list.id }

                    ListRow(
                        list = list,
                        activeListId = activeListId,
                        itemCount = itemCount,
                        onEdit = onEdit,
                        onDelete = onDelete
                    )
                }
            }

            // ---------------- FOOTER ----------------

            item {

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // ---------------- LINKS: DELETE ----------------

                    TextButton(
                        onClick = { viewModel.showDeleteAllConfirm() },
                        enabled = !state.isDeletingAll,
                        modifier = Modifier
                            .background(
                                color = BrandOlive,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = BrandGrey
                        )

                        Spacer(Modifier.width(6.dp))

                        Text(
                            text = "Alle Listen löschen",
                            color = BrandBlack
                        )
                    }

                    // ---------------- RECHTS: ADD ----------------

                    TextButton(
                        onClick = onCreateNewList,
                        modifier = Modifier
                            .background(
                                color = BrandOlive,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = BrandGrey
                        )

                        Spacer(Modifier.width(6.dp))

                        Text(
                            text = "Neue Liste(n) hinzufügen",
                            color = BrandBlack
                        )
                    }
                }

                if (state.showDeleteAllConfirm){
                    DeleteAllListsScreen(
                        onConfirm = { viewModel.confirmDeleteAll() },
                        onDismiss = { viewModel.dismissDeleteAllConfirm() }
                    )
                }

                Spacer(Modifier.height(32.dp))

            }
        }
    }
}

@Composable
private fun ListRow(
    list: ShoppingList,
    activeListId: String?,
    itemCount: Int,
    onEdit: (ShoppingList) -> Unit,
    onDelete: (ShoppingList) -> Unit
) {

    val isActive = list.id == activeListId

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->

            if (value == SwipeToDismissBoxValue.EndToStart) {

                onDelete(list)
            }

            false
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

        Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isActive) 8.dp else 2.dp
            ),
                colors = CardDefaults.cardColors(
                    containerColor =
                        if (isActive) BrandGreen else BrandOlive
                ),
                shape = RoundedCornerShape(16.dp)
            ) {

            val bodyFontSize =
                MaterialTheme.typography.bodyMedium.fontSize

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
                                .background(BrandBlack)
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

                        val titleText = remember(
                            list.name,
                            itemCount,
                            bodyFontSize
                        ) {
                            buildAnnotatedString {

                                append(list.name)
                                append("    ")

                                withStyle(
                                    style = SpanStyle(
                                        fontSize = bodyFontSize,
                                        color = BrandBlack
                                    )
                                ) {
                                    append(
                                        if (itemCount == 1)
                                            "(1 Artikel)"
                                        else
                                            "(${itemCount} Artikel)"
                                    )
                                }
                            }
                        }

                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text =
                                if (isActive) "Aktive Liste"
                                else "Tippen zum Öffnen",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandBlack
                        )
                    }
                }
            }
    }
}