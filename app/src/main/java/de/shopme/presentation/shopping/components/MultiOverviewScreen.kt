package de.shopme.presentation.shopping.components

import android.util.Log

import androidx.compose.runtime.*

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material.icons.filled.Add

import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.dp

import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.model.ShoppingList
import de.shopme.presentation.state.ShoppingViewState
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.presentation.screens.DeleteAllListsScreen
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

    val customLists = lists.filter { it.storeTypes.isEmpty() }
    val storeLists = lists.filter { it.storeTypes.isNotEmpty() }

    val state by viewModel.state.collectAsState()



    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        if (state.isDeletingAll) {
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

                items(
                    items = lists,
                    key = { it.id }
                ) { list ->

                    val items by viewModel
                        .itemsForList(list.id)
                        .collectAsState(initial = emptyList())

                    ListRow(
                        viewState = viewState,
                        list = list,
                        activeListId = activeListId,
                        itemCount = items.size,
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

                items(
                    items = storeLists,
                    key = { it.id }
                ) { list ->

                    val items by viewModel
                        .itemsForList(list.id)
                        .collectAsState(initial = emptyList())

                    ListRow(
                        viewState = viewState,
                        list = list,
                        activeListId = activeListId,
                        itemCount = items.size,
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

                if (state.showDeleteAllConfirm) {
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
    viewState: ShoppingViewState,
    list: ShoppingList,
    activeListId: String?,
    itemCount: Int,
    onEdit: (ShoppingList) -> Unit,
    onDelete: (ShoppingList) -> Unit
) {

    val isActive = list.id == activeListId

    var isRemoving by remember(list.id) { mutableStateOf(false) }

    var deleteTriggered by remember(list.id) { mutableStateOf(false) } // 🔥 NEU

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->

            if (value == SwipeToDismissBoxValue.EndToStart && !deleteTriggered) {

                deleteTriggered = true // 🔥 verhindert mehrfaches Triggern

                Log.d("SWIPE_DEBUG", "DELETE DIRECT | list=${list.id}")

                onDelete(list)

                false // 🔥 wichtig: kein hängenbleiben
            } else {
                false
            }
        }
    )

    // 👉 bleibt unverändert
    LaunchedEffect(isRemoving) {
        if (isRemoving) {
            Log.d("SWIPE_DEBUG", "Start remove animation | list=${list.id}")

            kotlinx.coroutines.delay(220)

            Log.d("SWIPE_DEBUG", "DELETE TRIGGERED | list=${list.id}")

            onDelete(list)
        }
    }

    LaunchedEffect(deleteTriggered) {
        if (deleteTriggered) {
            dismissState.reset()
        }
    }

    AnimatedVisibility(
        visible = !isRemoving,
        exit = shrinkVertically() + fadeOut()
    ) {

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
                            if (isActive) BrandGreen else BrandOlive
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
                                            color = BrandBlack
                                        )
                                    ) {
                                        append("(${itemCount} Artikel)")
                                    }
                                },
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
    }
}