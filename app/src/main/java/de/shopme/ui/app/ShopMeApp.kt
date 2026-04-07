package de.shopme.ui.app

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.shopme.R
import de.shopme.app.MainActivity
import de.shopme.presentation.action.ShoppingAction
import de.shopme.presentation.event.ShopEvent
import de.shopme.presentation.state.ShoppingScreenMode
import de.shopme.presentation.shopping.components.MultiOverviewScreen
import de.shopme.presentation.shopping.components.StoreSelectionDialog
import de.shopme.data.input.speech.SpeechController
import de.shopme.domain.service.CatalogService
import de.shopme.presentation.effect.UIEffect
import de.shopme.ui.components.ShoppingContent
import de.shopme.presentation.screens.WelcomeScreen
import de.shopme.ui.theme.BrandGreen
import de.shopme.presentation.navigation.Screen
import de.shopme.presentation.screens.ChooseListsScreen
import de.shopme.ui.navigation.toScreen
import de.shopme.ui.illustration.buttons.AddActionButton
import de.shopme.ui.illustration.buttons.CloseActionButton
import de.shopme.ui.illustration.icons.shopicons.StoreIcon
import de.shopme.presentation.screens.InviteScreen
import de.shopme.presentation.screens.ProfileScreen
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.ui.theme.BrandOlive


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

    val uiState = viewState.uiState
    val userLists = viewState.lists
    val activeList = viewState.activeList
    val groupedItems = viewState.groupedItems
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showChooseLists by remember { mutableStateOf(false) }

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

                is UIEffect.StartGoogleSignIn -> {
                    val activity = context as MainActivity
                    activity.startGoogleLogin()
                }

                else -> Unit
            }
        }
    }

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
        vm.shareEvent.collect { link ->
            shareLink(link)
        }
    }

    val showWelcomeDialog = viewState.showWelcomeDialog

    val blurWelcome by animateDpAsState(
        targetValue = if (showWelcomeDialog) 10.dp else 0.dp,
        animationSpec = tween(300),
        label = "welcomeBlur"
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(state.showProfileScreen) {
        if (state.showProfileScreen) {
            showChooseLists = false
        }
    }

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
                        SnackbarHost(hostState = snackbarHostState)
                    }
                },
                containerColor = Color.Transparent,

                topBar = {

                    val showAccountAction by vm.showAccountAction.collectAsState()
                    val isAnonymous by vm.isAnonymous.collectAsState()

                    CenterAlignedTopAppBar(

                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = BrandGreen,
                            titleContentColor = Color.Black
                        ),

                        navigationIcon = {
                            if (showAccountAction) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable {
                                            if (isAnonymous) vm.openProfileScreen()
                                        }
                                        .padding(start = 8.dp)
                                ) {
                                    Icon(Icons.Default.AccountCircle, null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        if (isAnonymous) "Profil erstellen" else "Profil"
                                    )
                                }
                            }
                        },

                        title = {
                            Text(activeList?.name ?: "ShopMe")
                        },

                        actions = {
                            if (userLists.isNotEmpty()) {

                                IconButton(onClick = {
                                    showChooseLists = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Listen teilen"
                                    )
                                }
                            }
                        }
                    )
                }

            ) { padding ->

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {

                    when (uiState.toScreen()) {

                        Screen.ListsOverview -> {
                            MultiOverviewScreen(
                                viewModel = vm,
                                lists = userLists,
                                activeListId = activeList?.id,
                                onEdit = { vm.editList(it) },
                                onDelete = { vm.deleteList(it) },
                                onCreateNewList = {
                                    vm.dispatch(ShoppingAction.StartMultiStoreCreation)
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

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {

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

                        Screen.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }

        if (
            state.showProfileScreen ||
            state.isJoining ||
            state.inviteListIds.isNotEmpty() ||
            showChooseLists ||
            (showWelcomeDialog && userLists.isEmpty())
        ) {

            when {

                state.showProfileScreen -> {
                    ProfileScreen(
                        onConfirm = vm::saveProfile,
                        onDismiss = vm::dismissProfileScreen,
                        onGoogleSignIn = vm::startGoogleSignIn
                    )
                }

                state.inviteListIds.isNotEmpty() && state.activeListId == null -> {

                    InviteScreen(
                        state = state,
                        onAccept = {
                            vm.acceptInvite(state.inviteListIds)
                        },
                        onDecline = {
                            vm.declineInvite()
                        }
                    )
                }

                showWelcomeDialog && userLists.isEmpty() -> {
                    WelcomeScreen(
                        onCreateFirstList = {
                            vm.dismissWelcomeDialog()
                            vm.startMultiStoreCreation()
                        }
                    )
                }
            }

            if (showChooseLists && !state.showProfileScreen) {
                ChooseListsScreen(
                    lists = userLists,
                    onConfirm = { selectedIds ->
                        showChooseLists = false
                        vm.onShareClicked(selectedIds)
                    },
                    onDismiss = {
                        showChooseLists = false
                    }
                )
            }
        }
    }
}