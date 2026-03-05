package de.shopme.ui
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.shopme.R
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.presentation.shopping.components.StoreSelectionDialog
import de.shopme.presentation.shopping.components.MultiOverviewScreen
import de.shopme.speech.SpeechController
import de.shopme.ui.components.ShoppingContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.draw.shadow
import de.shopme.presentation.shopping.ShoppingUiState
import de.shopme.ui.theme.BrandGreen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopMeApp(
    vm: ShoppingViewModel,
    speechController: SpeechController
) {
    val activeList by vm.activeList.collectAsStateWithLifecycle()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val selectedStores by vm.selectedStores.collectAsStateWithLifecycle()
    val userLists by vm.userLists.collectAsStateWithLifecycle()
    val groupedItems by vm.groupedItems.collectAsStateWithLifecycle()
    val snackbarMessage by vm.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val itemCount = groupedItems.values.sumOf { it.size }
    var showListSelector by remember { mutableStateOf(false) }
    var showWelcomeDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(userLists) {
        if (userLists.isEmpty()) {
            showWelcomeDialog = true
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_shopme),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.5f),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )
        val hasLists = userLists.isNotEmpty()
        val showListSelectorButton =
            uiState == ShoppingUiState.Normal ||
                    (uiState == ShoppingUiState.MultiOverview && hasLists)
        val showCreateTitle =
            uiState == ShoppingUiState.MultiSelect ||
                    uiState == ShoppingUiState.MultiSelect
        Scaffold(
            snackbarHost = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        snackbar = { data ->

                            AnimatedVisibility(
                                visible = true,
                                enter =
                                    slideInVertically(
                                        initialOffsetY = { it },
                                        animationSpec = tween(
                                            durationMillis = 350,
                                            easing = FastOutSlowInEasing
                                        )
                                    ) +
                                            fadeIn(
                                                animationSpec = tween(
                                                    durationMillis = 250
                                                )
                                            ),
                                exit = fadeOut(
                                    animationSpec = tween(
                                        durationMillis = 200
                                    )
                                )
                            ) {

                                Snackbar(
                                    snackbarData = data,
                                    containerColor = BrandGreen.copy(alpha = 0.95f),
                                    contentColor = Color.White,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .widthIn(max = 420.dp)
                                        .padding(horizontal = 16.dp)
                                )

                            }
                        }
                    )
                }
            },
            containerColor = Color.Transparent,
            topBar = {
                val showListHeader =
                    (uiState == ShoppingUiState.Normal) ||
                            (uiState == ShoppingUiState.MultiOverview && userLists.isNotEmpty())
                val titleText =
                    when (uiState) {
                        ShoppingUiState.MultiSelect -> "Supermarkt wählen"
                        else -> "ShopMe"
                    }
                CenterAlignedTopAppBar(
                    title = {
                        AnimatedContent(
                            targetState = uiState,
                            label = "TopBarAnimation"
                        ) { _ ->
                            if (showListHeader && activeList != null) {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                    modifier = Modifier.clickable { showListSelector = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        activeList!!.storeTypes.firstOrNull()?.let { store ->
                                            Image(
                                                painter = painterResource(id = store.logoRes),
                                                contentDescription = store.displayName,
                                                modifier = Modifier.size(26.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        Column {
                                            Text(
                                                text = activeList!!.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "$itemCount Artikel",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Liste wechseln",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = titleText,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = BrandGreen,
                        titleContentColor = Color.White
                    )
                )
            },
            floatingActionButton = {
                if (uiState == ShoppingUiState.Normal ||
                    (uiState == ShoppingUiState.MultiOverview && userLists.isNotEmpty())
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { vm.startMultiStoreCreation() },
                        icon = {
                            Image(
                                painter = painterResource(id = R.drawable.plus_icon_48),
                                contentDescription = "Neue Liste",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        text = { Text("Weitere Liste") },
                        shape = RoundedCornerShape(24.dp),
                        containerColor = BrandGreen,
                        contentColor = Color.Black,
                        modifier = Modifier
                            .padding(bottom = 12.dp)   // ⭐ Abstand zum Screenrand
                            .shadow(12.dp, RoundedCornerShape(24.dp))
                        ,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 8.dp,   // ⭐ stärkeres Schweben
                            pressedElevation = 14.dp
                        )
                    )
                }
            }
        ) { padding ->
            // ? DEIN BESTEHENDER COLUMN BLOCK BLEIBT HIER
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                when (uiState) {
                    ShoppingUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Lade Listen...")
                            }
                        }
                    }
                    ShoppingUiState.MultiOverview -> {
                        MultiOverviewScreen(
                            lists = userLists,
                            activeListId = vm.activeList.collectAsState().value?.id,
                            onEdit = { vm.editList(it) },
                            onDelete = { vm.deleteList(it) },
                            onCreateNewList = { vm.startMultiStoreCreation() }
                        )
                    }
                    ShoppingUiState.Normal -> {
                        ShoppingContent(
                            vm = vm,
                            speechController = speechController
                        )
                    }
                    ShoppingUiState.MultiSelect -> {
                        StoreSelectionDialog(
                            selectedStores = selectedStores,
                            existingStores = userLists.flatMap { it.storeTypes },
                            onToggle = vm::toggleStore,
                            onConfirm = { customLists ->
                                vm.confirmStoreSelection(customLists)
                            },
                            onDismiss = { vm.cancelMultiCreation() }
                        )
                    }
                }
            }
        }
        // ============================================
        // LIST SELECTOR BOTTOM SHEET
        // ============================================
        if (showListSelector) {
            ModalBottomSheet(
                onDismissRequest = { showListSelector = false }
            ) {
                userLists.forEach { list ->
                    ListItem(
                        headlineContent = { Text(list.name) },
                        leadingContent = {
                            list.storeTypes.firstOrNull()?.let { store ->
                                Image(
                                    painter = painterResource(id = store.logoRes),
                                    contentDescription = store.displayName,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            vm.editList(list)
                            showListSelector = false
                        }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showWelcomeDialog) {

        AlertDialog(
            onDismissRequest = { },

            confirmButton = {

                TextButton(
                    onClick = {
                        showWelcomeDialog = false
                        vm.startMultiStoreCreation()
                    }
                ) {
                    Text("Erste Liste erstellen")
                }
            },

            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(
                        painter = painterResource(R.drawable.store_icon76),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text("Willkommen bei ShopMe")
                }
            },

            text = {

                Column {

                    Text(
                        text = "Erstelle deine erste Einkaufsliste\nfür deinen Lieblingsmarkt.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Du kannst später jederzeit weitere Listen hinzufügen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

}
