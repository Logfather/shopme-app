package de.shopme.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.shopme.data.input.speech.SpeechController
import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.service.CatalogService
import de.shopme.presentation.action.ShoppingAction
import de.shopme.presentation.event.ShopEvent
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.ui.theme.AppButtonDefaults
import de.shopme.ui.theme.BrandOlive
import de.shopme.ui.theme.CategoryColors

sealed interface ListRow {
    data class Header(val category: String) : ListRow
    data class Item(val item: ShoppingItem) : ListRow
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingContent(
    vm: ShoppingViewModel,
    speechController: SpeechController,
    catalogService: CatalogService
) {

    val state by vm.state.collectAsStateWithLifecycle()

    val groupedItems =
        state.items
            .filter { it.deletedAt == null }
            .groupBy { it.category }

    val categoryEntries = remember(groupedItems) {
        groupedItems.entries.toList()
    }

    var lastUndoMessage by remember { mutableStateOf<String?>(null) }

    var lastDeletedItem by remember { mutableStateOf<ShoppingItem?>(null) }
    var text by rememberSaveable { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    // Undo Snackbar
    LaunchedEffect(lastDeletedItem) {

        val result = snackbarHostState.showSnackbar(
            message = "Item gelöscht",
            actionLabel = "Rückgängig",
            duration = SnackbarDuration.Short
        )

        if (result == SnackbarResult.ActionPerformed) {
            vm.onEvent(ShopEvent.List.UndoLastAction)
        }

        lastDeletedItem = null
    }

    LaunchedEffect(lastUndoMessage) {

        val message = lastUndoMessage ?: return@LaunchedEffect

        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = "Rückgängig",
            duration = SnackbarDuration.Short
        )

        if (result == SnackbarResult.ActionPerformed) {
            vm.onEvent(ShopEvent.List.UndoLastAction)
        }

        lastUndoMessage = null
    }

    // 👉 rows MUSS außerhalb LazyColumn sein
    val rows = remember(categoryEntries) {
        buildList<ListRow> {
            categoryEntries.forEach { entry ->
                add(ListRow.Header(entry.key))
                entry.value.forEach { item ->
                    add(ListRow.Item(item))
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->

                    Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(16.dp),
                        containerColor = BrandOlive,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        actionColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (text.isNotBlank()) {
                                vm.onEvent(ShopEvent.Item.Add(text))
                                text = ""
                                keyboardController?.hide()
                            }
                        }
                    )
                )

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            vm.onEvent(ShopEvent.Item.Add(text))
                            text = ""
                        }
                    },
                    modifier = Modifier.height(56.dp),
                    colors = AppButtonDefaults.primary()
                ) {
                    Text("Hinzufügen")
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState
            ) {

                items(
                    items = rows,
                    key = { row ->
                        when (row) {
                            is ListRow.Header -> "header_${row.category}"
                            is ListRow.Item -> row.item.id
                        }
                    }
                ) { row ->

                    when (row) {

                        is ListRow.Header -> {
                            Text(
                                text = row.category,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }

                        is ListRow.Item -> {

                            val item = row.item

                            val dismissState = remember(item.id) {
                                mutableStateOf(false)
                            }

                            if (!dismissState.value) {

                                val swipeState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value: SwipeToDismissBoxValue ->
                                        if (value == SwipeToDismissBoxValue.EndToStart) {
                                            vm.onEvent(ShopEvent.Item.Delete(item))
                                            lastDeletedItem = item
                                            dismissState.value = true
                                            true
                                        } else false
                                    }
                                )

                                SwipeToDismissBox(
                                    state = swipeState,
                                    backgroundContent = {}
                                ) {

                                    SupermarketItemRow(
                                        item = item,
                                        categoryColor = CategoryColors[item.category]
                                            ?: MaterialTheme.colorScheme.onSurfaceVariant,
                                        onToggle = {
                                            vm.onEvent(ShopEvent.Item.Toggle(item))
                                        },
                                        onDelete = {
                                            vm.onEvent(ShopEvent.Item.Delete(item))
                                            lastDeletedItem = item
                                        },
                                        onRetry = { id ->
                                            vm.onEvent(ShopEvent.Item.RetrySync(id))
                                        },
                                        onUpdate = { newText ->

                                            vm.onEvent(
                                                ShopEvent.Item.Update(item, newText)
                                            )

                                            lastUndoMessage = "Item geändert"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    vm.dispatch(ShoppingAction.CancelMultiCreation)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = AppButtonDefaults.primary()
            ) {
                Text("Liste erstellen fertig")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}