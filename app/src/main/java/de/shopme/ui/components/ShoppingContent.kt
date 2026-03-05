package de.shopme.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.speech.SpeechController
import de.shopme.ui.ShopEvent
import de.shopme.ui.theme.AppButtonDefaults
import de.shopme.ui.theme.CategoryColors

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
    speechController: SpeechController
) {

    val groupedItems by vm.groupedItems.collectAsStateWithLifecycle()
    val categoryEntries = remember(groupedItems) {
        groupedItems.entries.toList()
    }
    val categories = remember(groupedItems) { groupedItems.keys.toList() }
    val listening by speechController.isListening.collectAsStateWithLifecycle()

    var text by rememberSaveable { mutableStateOf("") }

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

        // =============================
        // Eingabezeile
        // =============================

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
                            vm.onEvent(ShopEvent.AddItem(text))
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
                        vm.onEvent(ShopEvent.AddItem(text))
                        text = ""
                    }
                },
                modifier = Modifier.height(56.dp),
                colors = AppButtonDefaults.primary()
            ) {
                Text("Hinzufügen")
            }
        }

        Spacer(Modifier.height(24.dp))

        // =============================
        // Liste
        // =============================

        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState
        ) {

            items(
                items = categoryEntries,
                key = { it.key }
            ) { entry ->

                val category = entry.key
                val itemsInCategory = entry.value

                val categoryColor =
                    CategoryColors[category]
                        ?: MaterialTheme.colorScheme.onSurfaceVariant

                // ⭐ Kategorie Header
                Text(
                    text = category,
                    color = categoryColor,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )

                itemsInCategory.forEach { item ->

                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                vm.onEvent(ShopEvent.DeleteItem(item))
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.secondary)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "Löschen",
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        },
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true
                    ) {
                        SupermarketItemRow(
                            item = item,
                            categoryColor = categoryColor,
                            onToggle = {
                                vm.onEvent(ShopEvent.ToggleItem(item))
                            },
                            onDelete = {
                                vm.onEvent(ShopEvent.DeleteItem(item))
                            },
                            onEdit = { newName ->
                                vm.onEvent(
                                    ShopEvent.UpdateItem(item, newName)
                                )
                            }
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                vm.onEvent(ShopEvent.CreateInvite(context))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = AppButtonDefaults.primary()
        ) {
            Text("Liste teilen")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { vm.onEvent(ShopEvent.ClearAll) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Liste löschen")
        }

        Spacer(Modifier.height(24.dp))
    }
}