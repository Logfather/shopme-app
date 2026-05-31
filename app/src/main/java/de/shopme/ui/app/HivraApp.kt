package de.shopme.ui.app

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import de.shopme.R
import de.shopme.data.input.speech.SpeechController
import de.shopme.data.sync.logging.UILog
import de.shopme.domain.service.CatalogService
import de.shopme.presentation.action.ShoppingAction
import de.shopme.presentation.event.ShopEvent
import de.shopme.presentation.navigation.Screen
import de.shopme.presentation.screens.ChooseListsScreen
import de.shopme.presentation.screens.InviteScreen
import de.shopme.presentation.screens.ProfileMode
import de.shopme.presentation.screens.ProfileScreen
import de.shopme.presentation.screens.WelcomeScreen
import de.shopme.presentation.shopping.components.MultiOverviewScreen
import de.shopme.presentation.shopping.components.StoreSelectionDialog
import de.shopme.presentation.state.ShoppingScreenMode
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.ui.components.CartoonLoader
import de.shopme.ui.components.ShoppingContent
import de.shopme.ui.icons.CheckFlagIcon
import de.shopme.ui.illustration.animations.ShareSuccessAnimation
import de.shopme.ui.illustration.animations.ShoppingBagAnimation
import de.shopme.ui.navigation.toScreen
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandGreen
import de.shopme.ui.theme.BrandGrey
import de.shopme.ui.theme.BrandOlive
import de.shopme.ui.theme.BrandRed
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HivraApp(
    vm: ShoppingViewModel,
    speechController: SpeechController,
    catalogService: CatalogService
) {

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

            vm.shareReturnTrigger.collect { trigger ->

                if (trigger > 0) {

                    // 🔥 KEY FIX
                    delay(1000)

                    vm.dispatch(
                        event = ShopEvent.List.StartSharing(
                            listId = vm.state.value.activeListId
                                ?: return@collect
                        )
                    )
                }
            }
        }
    }

    val viewState by vm.viewState.collectAsStateWithLifecycle()

    if (viewState.uiState == ShoppingScreenMode.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CartoonLoader()
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
    val isGoogleUser by vm.isGoogleUser.collectAsStateWithLifecycle()


    fun shareLink(link: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, link)
        }

        context.startActivity(
            Intent.createChooser(intent, "Liste teilen")
        )
    }

    LaunchedEffect(lifecycleOwner) {

        lifecycleOwner.lifecycle.repeatOnLifecycle(
            Lifecycle.State.STARTED
        ) {

            vm.shareEvent.collect { link ->
                shareLink(link)
            }
        }
    }

    val showWelcomeDialog = viewState.showWelcomeDialog

    val blurWelcome by animateDpAsState(
        targetValue = if (showWelcomeDialog) 10.dp else 0.dp,
        animationSpec = tween(300),
        label = "welcomeBlur"
    )

    val state by vm.state.collectAsStateWithLifecycle()

    if (state.showSaveChoice) {

        AlertDialog(
            onDismissRequest = {
                vm.hideSaveChoice()
            },

            containerColor = BrandOlive,

            title = {
                Text(
                    text = "Wie speichern?",
                    color = BrandBlack
                )
            },

            text = {
                Text(
                    text = "Möchtest du nur ein App-Profil erstellen oder dich mit einem bestehenden Konto verknüpfen?",
                    color = BrandBlack
                )
            },

            confirmButton = {
                Column {

                    // ---------------- PROFIL SPEICHERN ----------------

                    OutlinedButton(
                        onClick = {
                            vm.confirmManualSave()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGreen,
                            contentColor = BrandBlack
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(
                            width = 2.dp,
                            color = BrandGrey
                        )
                    ) {
                        Text("Profil speichern")
                    }

                    Spacer(Modifier.height(8.dp))

                    // ---------------- GOOGLE ----------------

                    OutlinedButton(
                        onClick = {
                            vm.confirmGoogleSave()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGreen,
                            contentColor = BrandBlack
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(
                            width = 2.dp,
                            color = BrandGrey
                        )
                    ) {
                        Text("Mit Google verknüpfen")
                    }

                    Spacer(Modifier.height(8.dp))
                }
            },

            dismissButton = {}
        )
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
                    .background(BrandBlack.copy(alpha = 0.35f))
            )

            Scaffold(
                snackbarHost = {
                    SnackbarHost(snackbarHostState) { data ->

                        Snackbar(
                            shape = RoundedCornerShape(16.dp),
                            containerColor = BrandRed,
                            contentColor = BrandBlack
                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {

                                TextButton(
                                    onClick = { data.performAction() }
                                ) {
                                    Text(
                                        text = data.visuals.actionLabel ?: "",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = BrandBlack
                                    )
                                }
                            }
                        }
                    }
                },
                containerColor = Color.Transparent,

                topBar = {

                    CenterAlignedTopAppBar(

                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = BrandGreen,
                            titleContentColor = BrandBlack
                        ),

                        navigationIcon = {

                            var showBadge by remember { mutableStateOf(false) }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable {
                                        vm.onEvent(ShopEvent.System.OpenProfileScreen)
                                    }
                                    .padding(start = 8.dp)
                            ) {

                                Icon(Icons.Default.AccountCircle, null)

                                Spacer(Modifier.width(6.dp))

                                Text(
                                    text = if (!state.hasProfile) {
                                        "Profil erstellen"
                                    } else {
                                        state.displayName ?: ""
                                    },
                                    color = BrandBlack,
                                    maxLines = 1
                                )

                                Spacer(Modifier.width(6.dp))

                                AnimatedVisibility(
                                    visible = showBadge,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {

                                    val scale by animateFloatAsState(
                                        targetValue = if (showBadge) 1.2f else 1f,
                                        animationSpec = tween(200)
                                    )

                                    Icon(
                                        imageVector = CheckFlagIcon,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .scale(scale),
                                        tint = BrandBlack
                                    )
                                }
                            }
                        },

                        title = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                //HivraNetworkIcon(
                                //    modifier = Modifier.size(28.dp)
                                //)
                            }
                        },

                        actions = {
                            if (userLists.isNotEmpty()) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { showChooseLists = true }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {

                                    Text(
                                        text = "Teilen",
                                        color = BrandBlack,
                                        maxLines = 1
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    Icon(Icons.Default.Share,
                                        contentDescription = "Listen teilen",
                                        tint = BrandBlack)
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
                    LaunchedEffect(
                        uiState.toScreen()
                    ) {

                        UILog.navigation(
                            uiState.toScreen().toString()
                        )
                    }

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

                                    onConfirm = { stores, customLists ->
                                        vm.dispatch(
                                            ShoppingAction.ConfirmStores(
                                                customLists = customLists
                                            )
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
                                CartoonLoader()
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

                    LaunchedEffect(
                        state.showProfileScreen
                    ) {

                        if (state.showProfileScreen) {

                            UILog.overlay(
                                "ProfileScreen"
                            )
                        }
                    }

                    ProfileScreen(
                        mode = if (state.hasProfile) ProfileMode.EDIT else ProfileMode.CREATE,
                        currentName = state.displayName,
                        hasProfile = state.hasProfile,
                        firstNameValue = state.firstName,
                        lastNameValue = state.lastName,
                        emailValue = state.email,
                        isGoogleUser = isGoogleUser,

                        onCreateProfile = { firstName, lastName, email, nickName ->

                            vm.onProfileCreated(firstName, lastName, email, nickName)
                        },

                        onUpdateProfile = { nickName, firstName, lastName, email ->
                            vm.updateUserProfileUnified(
                                nickName = nickName,
                                firstName = firstName,
                                lastName = lastName,
                                email = email
                            )
                        },

                        onGoogleSignIn = {
                            vm.startGoogleSignIn()
                        },

                        onShowSaveChoice = { nick, first, last, mail ->
                            vm.showSaveChoice(
                                nickName = nick,
                                firstName = first,
                                lastName = last,
                                email = mail
                            )
                        },

                        onDismiss = {
                            vm.dismissProfileScreen()
                        },

                        onUnlinkGoogle = {
                            vm.unlinkGoogleAccount()
                        },

                        onLinkGoogle = {
                            vm.confirmGoogleSave()
                        },

                        onDeleteAccount = {
                            vm.deleteAccount()
                        }
                    )
                }

                state.isJoining -> {

                    LaunchedEffect(
                        state.isJoining
                    ) {

                        if (state.isJoining) {

                            UILog.overlay(
                                "JoinOverlay"
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            LinearProgressIndicator()

                            Text(
                                text = "Einladung wird verarbeitet..."
                            )
                        }
                    }
                }

                state.inviteListIds.isNotEmpty() && state.showInviteDialog -> {

                    LaunchedEffect(
                        state.inviteListIds,
                        state.activeListId
                    ) {

                        if (
                            state.inviteListIds.isNotEmpty() &&
                            state.activeListId == null
                        ) {

                            UILog.overlay(
                                "InviteScreen"
                            )
                        }
                    }

                    InviteScreen(
                        state = state,
                        onAccept = {
                            vm.acceptCurrentInvite()
                        },
                        onDecline = {
                            vm.declineInvite()
                        }
                    )
                }

                showWelcomeDialog && userLists.isEmpty() -> {

                    LaunchedEffect(
                        showWelcomeDialog,
                        userLists
                    ) {

                        if (
                            showWelcomeDialog &&
                            userLists.isEmpty()
                        ) {

                            UILog.overlay(
                                "WelcomeScreen"
                            )
                        }
                    }

                    WelcomeScreen(
                        onCreateFirstList = {
                            vm.dismissWelcomeDialog()
                            vm.startMultiStoreCreation()
                        }
                    )
                }
            }

            if (showChooseLists && !state.showProfileScreen) {

                LaunchedEffect(
                    showChooseLists
                ) {

                    if (showChooseLists) {

                        UILog.overlay(
                            "ChooseListsScreen"
                        )
                    }
                }

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



        // ============================================================
        // 🌀 GLOBAL SORTING OVERLAY (NEU)
        // ============================================================
        if (
            state.isSorting ||
            state.isDeletingAll ||
            state.isSharing ||
            state.showShareSuccess
        ) {

            LaunchedEffect(
                state.isSorting,
                state.isDeletingAll,
                state.isSharing,
                state.showShareSuccess
            ){

                when {

                    // Overlay priority:
                    // Sorting > Delete > Sharing

                    state.isSorting -> {

                        UILog.overlay(
                            "SortingOverlay"
                        )
                    }

                    state.isDeletingAll -> {

                        UILog.overlay(
                            "DeleteOverlay"
                        )
                    }

                    state.isSharing -> {

                        UILog.overlay(
                            "SharingOverlay"
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BrandBlack.copy(alpha = 0.25f))
                    .pointerInput(Unit) {},
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    when {
                        state.isSorting || state.isDeletingAll -> {
                            ShoppingBagAnimation(
                                isVisible = true,
                                isDeleting = state.isDeletingAll
                            )
                        }

                        state.isSharing -> {
                            // 🔄 Ladezustand während Vorbereitung / Profil / Intent-Aufbau
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        state.showShareSuccess -> {
                            ShareSuccessAnimation(
                                visible = true,
                                onFinished = {
                                    vm.dispatch(event = ShopEvent.List.FinishSharing)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}