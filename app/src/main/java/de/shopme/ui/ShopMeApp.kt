package de.shopme.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.shopme.R
import de.shopme.presentation.ShoppingViewModel
import de.shopme.speech.SpeechController
import de.shopme.ui.components.RecordingButton
import de.shopme.ui.components.SupermarketItemRow
import de.shopme.ui.theme.AppButtonDefaults
import de.shopme.ui.theme.CategoryColors
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopMeApp(
    vm: ShoppingViewModel,
    speechController: SpeechController
) {

    val groupedItems by vm.groupedItems.collectAsStateWithLifecycle()
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

    Box(modifier = Modifier.fillMaxSize()) {

        // =============================
        // Hintergrundbild (50%)
        // =============================

        Image(
            painter = painterResource(id = R.drawable.bg_shopme),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.5f),
            contentScale = ContentScale.Crop
        )

        // Optionales Overlay für Lesbarkeit
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("ShopMe") },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
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
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.secondary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                            focusedTextColor = MaterialTheme.colorScheme.onSecondary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
                            cursorColor = MaterialTheme.colorScheme.onSecondary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
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
                // Mikrofon
                // =============================

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    RecordingButton(
                        isRecording = listening,
                        onClick = {
                            if (listening) {
                                speechController.stop()
                            } else {
                                val granted =
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                if (granted) {
                                    speechController.start()
                                } else {
                                    permissionLauncher.launch(
                                        Manifest.permission.RECORD_AUDIO
                                    )
                                }
                            }
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))

                // =============================
                // Liste
                // =============================

                val fallbackCategoryColor =
                    MaterialTheme.colorScheme.onSurfaceVariant

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = listState
                ) {

                    groupedItems.forEach { entry ->

                        val category = entry.key
                        val itemsInCategory = entry.value

                        val categoryColor =
                            CategoryColors[category] ?: fallbackCategoryColor

                        item(key = "header_$category") {
                            Text(
                                text = category,
                                color = categoryColor,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }

                        itemsIndexed(
                            items = itemsInCategory,
                            key = { _, item -> item.id }
                        ) { _, item ->

                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        vm.onEvent(ShopEvent.DeleteItem(item))
                                        true
                                    } else {
                                        false
                                    }
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
                                            color = MaterialTheme.colorScheme.onSecondary,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                },
                                enableDismissFromStartToEnd = false, // 🔥 Nur nach links
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
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Liste teilen")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { vm.onEvent(ShopEvent.ClearAll) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Liste löschen")
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}