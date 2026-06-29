package de.shopme.presentation.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.shopme.app.services.AppServices
import de.shopme.data.input.speech.SpeechController
import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.data.sync.logging.SyncLog
import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.service.CatalogService
import de.shopme.presentation.action.ShoppingAction
import de.shopme.presentation.components.ShopBuddy
import de.shopme.presentation.components.ShopBuddyMood
import de.shopme.presentation.components.ShopBuddyState
import de.shopme.presentation.event.ShopEvent
import de.shopme.presentation.state.ShoppingScreenMode
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.tools.knowledge.dimension.explorer.KnowledgeExplorerProvider
import de.shopme.ui.components.SupermarketItemRow
import de.shopme.ui.components.button.ShopBuddyButton
import de.shopme.ui.theme.AppButtonDefaults
import de.shopme.ui.theme.BrandCreme
import de.shopme.ui.theme.BrandGreen
import de.shopme.ui.theme.BrandOlive
import de.shopme.ui.theme.CategoryColors
import kotlinx.coroutines.delay


sealed interface ListRow {
    data class Header(val category: String) : ListRow
    data class Item(val item: ShoppingItem) : ListRow
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(

    vm: ShoppingViewModel,

    services: AppServices,

    speechController: SpeechController,

    catalogService: CatalogService,

    knowledgeExplorerProvider: KnowledgeExplorerProvider

) {

    val state by vm.state.collectAsStateWithLifecycle()

    val nutritionInsightService =
        services.shopBuddy.nutritionInsightService

    val duplicateItems = state.items
        .groupBy { it.id }
        .filter { it.value.size > 1 }

    if (duplicateItems.isNotEmpty()) {
        duplicateItems.forEach { (id, list) ->
            SyncLog.guard(
                "Duplicate item detected | itemId=$id | count=${list.size}"
            )
        }
    }

    val productionNutritionPipeline =
        services.nutrition.pipeline

    val context = LocalContext.current

    // ============================================================
    // ✅ MIC PERMISSION CONTROLLER
    // ============================================================

    var showMicPermissionWarning by remember {
        mutableStateOf(false)
    }

    var permissionDeniedCount by remember {
        mutableIntStateOf(0)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            RuntimeLog.runtime(
                "Permission already granted=$granted"
            )
            if (granted) {

                permissionDeniedCount = 0
                showMicPermissionWarning = false

                speechController.start()

            } else {

                permissionDeniedCount++

                showMicPermissionWarning = true
            }
        }

    val speechModeEnabled by
    speechController.speechModeEnabled
        .collectAsStateWithLifecycle()

    val groupedItems =
        state.items
            .filter { it.deletedAt == null }
            .sortedBy { it.createdAt } // 🔥 STABIL
            .groupBy { it.category }

    val categoryEntries = remember(groupedItems) {
        groupedItems
            .toSortedMap() // 🔥 stabile Kategorien
            .entries
            .toList()
    }

    var lastDeletedItem by remember { mutableStateOf<ShoppingItem?>(null) }
    var lastUndoMessage by remember { mutableStateOf<String?>(null) }

    var text by rememberSaveable { mutableStateOf("") }

    val suggestions = remember(text) {

        if (text.length < 2) {
            emptyList()
        } else {
            catalogService.autocomplete(text)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()


    // ============================================================
    // ✅ STATE CONTROLLER SHOP BUDDY
    // ============================================================

    val infiniteTransition =
        rememberInfiniteTransition(
            label = "buddyPulse"
        )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 900
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val buddyState = remember(
        speechModeEnabled,
        showMicPermissionWarning,
        permissionDeniedCount
    ) {

        when {

            showMicPermissionWarning &&
                    permissionDeniedCount <= 1 -> {

                ShopBuddyState(
                    mood = ShopBuddyMood.Warning,
                    text = "Für die Spracheingabe benötige ich Zugriff auf dein Mikrofon."
                )
            }

            showMicPermissionWarning &&
                    permissionDeniedCount > 1 -> {

                ShopBuddyState(
                    mood = ShopBuddyMood.Warning,
                    text =
                        "Mikrofonzugriff deaktiviert.\n\nBitte aktiviere ihn in den App-Einstellungen."
                )
            }

            speechModeEnabled -> {

                ShopBuddyState(
                    mood = ShopBuddyMood.Listening,
                    text = "Ich höre zu..."
                )
            }

            else -> {

                ShopBuddyState(
                    mood = ShopBuddyMood.Idle,
                    text = "Bereit."
                )
            }
        }
    }

    LaunchedEffect(showMicPermissionWarning) {

        if (showMicPermissionWarning) {

            delay(6000)

            showMicPermissionWarning = false
        }
    }

    // ============================================================
    // ✅ SPEECH CONTROLLER
    // ============================================================

    DisposableEffect(speechController) {

        speechController.setResultListener { spokenText ->

            RuntimeLog.runtime(
                "Listener received: $spokenText"
            )

            vm.onEvent(
                ShopEvent.Speech.AddItemFromSpeech(
                    spokenText
                )
            )
        }

        onDispose {

            speechController.setResultListener { }
        }
    }

    // ============================================================
    // ✅ UNDO DELETE
    // ============================================================
    LaunchedEffect(lastDeletedItem) {

        val item = lastDeletedItem ?: return@LaunchedEffect

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

    // ============================================================
    // ✅ UNDO UPDATE
    // ============================================================
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


            // ============================================================
            // ✅ INPUT (FIXED)
            // ============================================================

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
                                vm.onEvent(ShopEvent.Item.Add(text)) // ✅ WICHTIG
                                text = ""
                                keyboardController?.hide()
                            }
                        }
                    )
                )

                Spacer(Modifier.width(8.dp))


                ShopBuddyButton(
                    text = "Hinzufügen",
                    onClick = {
                        if (text.isNotBlank()) {
                            vm.onEvent(ShopEvent.Item.Add(text)) // ✅ WICHTIG
                            text = ""
                        }
                    }
                )

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = {

                        RuntimeLog.runtime(
                            "Mic button pressed | speechModeEnabled=$speechModeEnabled"
                        )


                        if (speechModeEnabled) {

                            speechController.stop()

                        } else {

                            val granted =
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                            if (granted) {

                                showMicPermissionWarning = false

                                speechController.start()

                            } else {

                                permissionLauncher.launch(
                                    Manifest.permission.RECORD_AUDIO
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .background(
                            BrandGreen,
                            RoundedCornerShape(12.dp)
                        )
                ) {

                    Icon(
                        imageVector =
                            if (speechModeEnabled) {
                                Icons.Default.Stop
                            } else {
                                Icons.Default.Mic
                            },
                        tint = Color.White,
                        contentDescription =
                            if (speechModeEnabled) {
                                "Spracherkennung stoppen"
                            } else {
                                "Spracherkennung starten"
                            }
                    )
                }

            }

            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                ShopBuddy(
                    mood = buddyState.mood,
                    modifier = Modifier
                        .size(96.dp)
                        .graphicsLayer(
                            scaleX =
                                if (buddyState.mood == ShopBuddyMood.Listening)
                                    scale
                                else
                                    1f,
                            scaleY =
                                if (buddyState.mood == ShopBuddyMood.Listening)
                                    scale
                                else
                                    1f
                        )
                )

                Spacer(
                    Modifier.width(12.dp)
                )

                Column {

                    Text(
                        text = buddyState.text,
                        modifier = Modifier
                            .background(
                                color = BrandCreme,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            )
                    )

                    if (
                        buddyState.mood == ShopBuddyMood.Warning &&
                        permissionDeniedCount > 1
                    ) {

                        Spacer(
                            Modifier.height(8.dp)
                        )

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            Button(
                                onClick = {

                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    ).apply {

                                        data = Uri.fromParts(
                                            "package",
                                            context.packageName,
                                            null
                                        )
                                    }

                                    context.startActivity(intent)
                                }
                            ) {
                                Text("Einstellungen öffnen")
                            }

                            Button(
                                onClick = {
                                    showMicPermissionWarning = false
                                    permissionDeniedCount = 0
                                }
                            ) {
                                Text("Nicht jetzt")
                            }
                        }
                    }
                }
            }

            if (
                suggestions.isNotEmpty() &&
                text.isNotBlank()
            ) {

                Spacer(Modifier.height(4.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(8.dp)
                        )
                ) {

                    suggestions.forEach { suggestion ->

                        Text(
                            text = suggestion.itemname,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                    text = suggestion.itemname

                                    keyboardController?.hide()
                                }
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 8.dp
                                )
                        )
                    }
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
                                    confirmValueChange = { dismissValue ->

                                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                            vm.onEvent(ShopEvent.Item.Delete(item))
                                            lastDeletedItem = item
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                )

                                SwipeToDismissBox(
                                    state = swipeState,
                                    backgroundContent = {}
                                ) {

                                    SupermarketItemRow(

                                        item = item,

                                        categoryColor =
                                            CategoryColors[item.category]
                                                ?: MaterialTheme.colorScheme.onSurfaceVariant,

                                        onToggle = {
                                            vm.onEvent(
                                                ShopEvent.Item.Toggle(item)
                                            )
                                        },

                                        onDelete = {

                                            if (
                                                swipeState.currentValue ==
                                                SwipeToDismissBoxValue.Settled
                                            ) {

                                                vm.onEvent(
                                                    ShopEvent.Item.Delete(item)
                                                )

                                                lastDeletedItem = item
                                            }
                                        },

                                        onRetry = { id ->

                                            vm.onEvent(
                                                ShopEvent.Item.RetrySync(id)
                                            )

                                        },

                                        onUpdate = { newText ->

                                            vm.onEvent(

                                                ShopEvent.Item.Update(
                                                    item,
                                                    newText
                                                )

                                            )

                                            lastUndoMessage = "Item geändert"

                                        },

                                        catalogService = catalogService,

                                        productionNutritionPipeline =
                                            productionNutritionPipeline,

                                        knowledgeExplorerProvider =
                                            knowledgeExplorerProvider,

                                        nutritionInsightService =
                                            nutritionInsightService

                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (state.screenMode is ShoppingScreenMode.Normal) {

                Button(
                    onClick = {
                        vm.dispatch(
                            ShoppingAction.FinishListCreation
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = AppButtonDefaults.primary()
                ) {
                    Text("Liste erstellen fertig")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}