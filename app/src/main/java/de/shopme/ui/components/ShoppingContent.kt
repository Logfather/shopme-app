package de.shopme.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle


import de.shopme.presentation.event.ShopEvent
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.data.input.speech.SpeechController
import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.service.CatalogService
import de.shopme.presentation.action.ShoppingAction
import de.shopme.ui.theme.AppButtonDefaults
import de.shopme.ui.theme.CategoryColors


sealed interface ListRow {
    data class Header(val category: String) : ListRow
    data class Item(val item: ShoppingItem) : ListRow
}

@Composable
fun ListHeader(
    storeLogo: Int,
    listName: String,
    itemCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(storeLogo),
                contentDescription = null,
                modifier = Modifier.size(42.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = listName,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "$itemCount Artikel",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingContent(
    vm: ShoppingViewModel,
    speechController: SpeechController,
    catalogService: CatalogService
) {

    val viewState by vm.viewState.collectAsStateWithLifecycle()
    val groupedItems = viewState.groupedItems

    val categoryEntries = remember(groupedItems) {
        groupedItems.entries.toList()
    }

    val listening by speechController.isListening.collectAsStateWithLifecycle()

    var text by rememberSaveable { mutableStateOf("") }

    // ------------------------------------------------------------
    // DEBOUNCE
    // ------------------------------------------------------------

    val debouncedText by produceState(initialValue = "", text) {
        kotlinx.coroutines.delay(120)
        value = text
    }

    // ------------------------------------------------------------
    // AUTOCOMPLETE
    // ------------------------------------------------------------

    val suggestions =
        remember(debouncedText) {

            if (debouncedText.length < 2) emptyList()
            else {

                val result =
                    catalogService
                        .autocomplete(debouncedText.lowercase())
                        .take(5)

                result
            }
        }

    val expanded = suggestions.isNotEmpty()

    val keyboardController = LocalSoftwareKeyboardController.current

    val listState = rememberLazyListState()

    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) speechController.start()
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {},
                modifier = Modifier.weight(1f)   // Gewicht hierhin verschieben
            ) {

                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                            enabled = true
                        )
                        .fillMaxWidth()
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

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {}
                ) {



                    suggestions.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.itemname) },
                            onClick = {

                                text = item.itemname

                                vm.onEvent(
                                    ShopEvent.Item.Add(item.itemname)
                                )

                                text = ""
                            }
                        )
                    }
                }
            }

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

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            RecordingButton(
                isRecording = listening,
                onClick = {
                    speechController.setResultListener { spokenText ->
                        vm.addItemsFromSpeech(spokenText)
                    }

                    if (listening) {
                        speechController.stop()
                    } else {
                        permissionLauncher.launch(
                            android.Manifest.permission.RECORD_AUDIO
                        )
                    }
                }
            )
        }

        Spacer(Modifier.height(24.dp))

        val rows = buildList {

            categoryEntries.forEach { entry ->

                add(ListRow.Header(entry.key))

                entry.value.forEach { item ->
                    add(ListRow.Item(item))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState
        ) {

            items(
                items = rows,
                key = {
                    when (it) {
                        is ListRow.Header -> "header_${it.category}"
                        is ListRow.Item -> it.item.id
                    }
                }
            ) { row ->

                when (row) {

                    is ListRow.Header -> {

                        val categoryColor =
                            CategoryColors[row.category]
                                ?: MaterialTheme.colorScheme.onSurfaceVariant

                        Text(
                            text = row.category,
                            color = categoryColor,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    }

                    is ListRow.Item -> {

                        val item = row.item

                        val categoryColor =
                            CategoryColors[item.category]
                                ?: MaterialTheme.colorScheme.onSurfaceVariant

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    vm.onEvent(ShopEvent.Item.Delete(item))
                                    true
                                } else false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {},
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true
                        ) {

                            SupermarketItemRow(
                                item = item,
                                categoryColor = categoryColor,
                                onToggle = {
                                    vm.onEvent(ShopEvent.Item.Toggle(item))
                                },
                                onDelete = {
                                    vm.onEvent(ShopEvent.Item.Delete(item))
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                vm.dispatch(
                    ShoppingAction.CancelMultiCreation
                )
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
