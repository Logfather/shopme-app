package de.shopme.ui.app

import android.content.Intent
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import android.content.Context
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.shopme.R
import de.shopme.app.MainActivity
import de.shopme.presentation.action.ShoppingAction
import de.shopme.presentation.event.ShopEvent
import de.shopme.presentation.state.ShoppingScreenMode
import de.shopme.presentation.shopping.components.MultiOverviewScreen
import de.shopme.presentation.shopping.components.StoreSelectionDialog
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.data.input.speech.SpeechController
import de.shopme.domain.model.StoreType
import de.shopme.domain.service.CatalogService
import de.shopme.presentation.effect.UIEffect
import de.shopme.ui.components.ShoppingContent
import de.shopme.ui.components.WelcomeDialog
import de.shopme.ui.theme.BrandGreen
import de.shopme.presentation.navigation.Screen
import de.shopme.ui.navigation.toScreen
import de.shopme.ui.illustration.buttons.AddActionButton
import de.shopme.ui.illustration.buttons.CloseActionButton
import de.shopme.ui.illustration.icons.shopicons.StoreIcon
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopMeApp(
    vm: ShoppingViewModel,
    speechController: SpeechController,
    catalogService: CatalogService
) {

    val viewState by vm.viewState.collectAsStateWithLifecycle()

    if (viewState.uiState == ShoppingScreenMode.Loading) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        return
    }

    val showAccountHint by vm.shouldShowAccountHint.collectAsState()
    val uiState = viewState.uiState
    val userLists = viewState.lists
    val activeList = viewState.activeList
    val groupedItems = viewState.groupedItems
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.effects.collect { effect ->

            when (effect) {

                is UIEffect.ShowUndo -> {

                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = "Rückgängig",
                        duration = SnackbarDuration.Short
                    )

                    if (result == SnackbarResult.ActionPerformed) {
                        vm.onEvent(ShopEvent.List.UndoLastAction)
                    }
                }

                else -> Unit
            }
        }
    }

    val context = LocalContext.current

    fun shareLink(link: String) {

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, link)
        }

        context.startActivity(
            Intent.createChooser(intent, "Liste teilen")
        )
    }

    LaunchedEffect(Unit) {
        vm.shareEvent.collect { link: String ->
            Log.d("SHARE_DEBUG", "UI received link: $link")
            shareLink(link)
        }
    }


    val showWelcomeDialog = viewState.showWelcomeDialog



    val itemCount = remember(groupedItems) {
        groupedItems.values.sumOf { it.size }
    }

    var showListSelector by remember { mutableStateOf(false) }

    val blurWelcome by animateDpAsState(
        targetValue = if (showWelcomeDialog) 10.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "welcomeBlur"
    )

    Box {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurWelcome)
        ) {

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

                                Snackbar(
                                    snackbarData = data,
                                    shape = RoundedCornerShape(16.dp),
                                    containerColor = BrandGreen.copy(alpha = 0.95f),
                                    contentColor = Color.Black,
                                    actionColor = Color.Black
                                )
                            }
                        )

                    }

                },

                containerColor = Color.Transparent,

                topBar = {

                    val titleText =
                        when (uiState.toScreen()) {
                            Screen.StoreSelection -> "Supermarkt wählen"
                            else -> "ShopMe"
                        }

                    val showAccountAction by vm.showAccountAction.collectAsState()
                    val isAnonymous by vm.isAnonymous.collectAsState()

                    CenterAlignedTopAppBar(

                        navigationIcon = {


                            if (showAccountAction) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable(enabled = isAnonymous) {
                                            (context as? MainActivity)?.startGoogleLogin()
                                        }
                                        .padding(start = 8.dp)
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Account Status",
                                        tint = Color.Black,
                                        modifier = Modifier.size(28.dp)
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    Text(
                                        text = if (isAnonymous) "Anonym" else "Verbunden",
                                        color = Color.Black,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        },

                        title = {

                            AnimatedContent(
                                targetState = uiState,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "TopBarAnimation"
                            ) { state ->

                                val showListHeader =
                                    (state == ShoppingScreenMode.Normal) ||
                                            (state == ShoppingScreenMode.MultiOverview && userLists.isNotEmpty())

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
                                            activeList.storeTypes.firstOrNull()?.let { store ->
                                                StoreIcon(
                                                    store = store,
                                                    modifier = Modifier.size(26.dp)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                            }

                                            Column {
                                                Text(
                                                    text = activeList.name,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(
                                                    text = "$itemCount Artikel",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }

                                            Spacer(Modifier.width(8.dp))

                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Liste wechseln"
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

                        actions = {
                            if (activeList != null) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Text(
                                        text = "Liste(n) teilen",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.Black,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    IconButton(onClick = {
                                        Log.d("SHARE_DEBUG", "Button clicked")
                                        vm.createInviteAndShare(activeList.id)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Liste teilen"
                                        )
                                    }
                                }
                            }
                        },

                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = BrandGreen,
                            titleContentColor = Color.Black
                        )

                    )

                },

                floatingActionButton = {

                    if (viewState.uiState == ShoppingScreenMode.MultiOverview &&
                        userLists.isNotEmpty()
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.End
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                ExtendedFloatingActionButton(

                                    onClick = {
                                        vm.onEvent(ShopEvent.List.DeleteAllLists)
                                    },

                                    icon = {
                                        CloseActionButton(
                                            modifier = Modifier.size(36.dp),
                                            onClick = {}
                                        )
                                    },

                                    text = { Text("Alle Listen löschen") },

                                    shape = RoundedCornerShape(24.dp),

                                    containerColor = BrandGreen,

                                    contentColor = Color.Black,

                                    elevation = FloatingActionButtonDefaults.elevation(
                                        defaultElevation = 8.dp,
                                        pressedElevation = 14.dp
                                    )
                                )

                                ExtendedFloatingActionButton(

                                    onClick = {
                                        vm.dispatch(
                                            ShoppingAction.StartMultiStoreCreation
                                        )
                                    },

                                    icon = {
                                        AddActionButton(
                                            modifier = Modifier.size(36.dp),
                                            onClick = {}
                                        )
                                    },

                                    text = { Text("Weitere Liste") },

                                    shape = RoundedCornerShape(24.dp),

                                    containerColor = BrandGreen,

                                    contentColor = Color.Black,

                                    elevation = FloatingActionButtonDefaults.elevation(
                                        defaultElevation = 8.dp,
                                        pressedElevation = 14.dp
                                    )
                                )
                            }
                        }
                    }
                }

            ) { padding ->

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .fillMaxSize()
                ) {

                    when (uiState.toScreen()) {

                        Screen.Loading -> {

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                    CircularProgressIndicator()

                                    Spacer(Modifier.height(12.dp))

                                    Text("Lade Listen...")

                                }

                            }

                        }

                        Screen.ListsOverview -> {

                            MultiOverviewScreen(
                                viewModel = vm,
                                lists = userLists,
                                activeListId = activeList?.id,
                                onEdit = { list -> vm.editList(list) },
                                onDelete = { list -> vm.deleteList(list) },
                                onCreateNewList = {
                                    vm.dispatch(
                                        ShoppingAction.StartMultiStoreCreation
                                    )
                                },
                                viewState = viewState
                            )

                        }

                        Screen.Items -> {

                            ShoppingContent(
                                vm = vm,
                                speechController = speechController,
                                catalogService = catalogService
                            )

                        }

                        Screen.StoreSelection -> {

                            val selectedStores =
                                (uiState as ShoppingScreenMode.MultiSelect).selectedStores

                            val existingStores = remember(userLists) {
                                userLists.flatMap { it.storeTypes }
                            }

                            StoreSelectionDialog(
                                selectedStores = selectedStores,
                                existingStores = existingStores,

                                onToggle = { store ->
                                    vm.dispatch(
                                        ShoppingAction.ToggleStore(store)
                                    )
                                },

                                onConfirm = { customLists ->
                                    vm.dispatch(
                                        ShoppingAction.ConfirmStores(customLists)
                                    )
                                },

                                onDismiss = {
                                    vm.dispatch(
                                        ShoppingAction.CancelMultiCreation
                                    )
                                }
                            )

                        }
                    }

                }

            }

            LaunchedEffect(showAccountHint) {

                if (showAccountHint) {

                    Log.d("ACCOUNT_HINT", "UI observed: $showAccountHint")

                    val result = snackbarHostState.showSnackbar(
                        message = "Verbinde Listen mit einem Account",
                        actionLabel = "Sichern",
                        duration = SnackbarDuration.Long
                    )

                    Log.d("ACCOUNT_HINT", "SNACKBAR RESULT: $result")

                    if (result == SnackbarResult.ActionPerformed) {
                        Log.d("ACCOUNT_HINT", "ACTION CLICKED")
                        (context as? MainActivity)?.startGoogleLogin()
                    }

                    // 🔥 ERST DANACH zurücksetzen
                    vm.shouldShowAccountHint.value = false
                    vm.showAccountAction.value = true
                }
            }

        }

        AnimatedVisibility(
            visible = showWelcomeDialog && userLists.isEmpty(),
            enter =
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(320)
                ) + fadeIn(),
            exit =
                slideOutVertically(
                    targetOffsetY = { it / 3 },
                    animationSpec = tween(220)
                ) + fadeOut()
        ) {

            if (showWelcomeDialog && userLists.isEmpty()) {

                WelcomeDialog(
                    onCreateFirstList = {
                        vm.dismissWelcomeDialog()
                        vm.startMultiStoreCreation()
                    }
                )
            }

        }

    }

}