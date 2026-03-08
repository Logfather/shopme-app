package de.shopme.ui.app

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.shopme.R
import de.shopme.presentation.action.ShoppingAction
import de.shopme.presentation.effect.UIEffect
import de.shopme.presentation.shopping.ShoppingUiState
import de.shopme.presentation.shopping.components.MultiOverviewScreen
import de.shopme.presentation.shopping.components.StoreSelectionDialog
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.speech.SpeechController
import de.shopme.ui.components.ShoppingContent
import de.shopme.ui.components.WelcomeDialog
import de.shopme.ui.theme.BrandGreen
import de.shopme.presentation.shopping.toScreen
import de.shopme.presentation.navigation.Screen
import de.shopme.presentation.shopping.toScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopMeApp(
    vm: ShoppingViewModel,
    speechController: SpeechController
) {

    val viewState by vm.viewState.collectAsStateWithLifecycle()

    val uiState = viewState.uiState
    val userLists = viewState.lists
    val activeList = viewState.activeList
    val groupedItems = viewState.groupedItems
    val snackbarMessage = viewState.snackbarMessage

    val snackbarHostState = remember { SnackbarHostState() }

    val showWelcomeDialog = viewState.showWelcomeDialog
    val showStoreSelectionDialog = viewState.showStoreSelectionDialog



    val itemCount = remember(groupedItems) {
        groupedItems.values.sumOf { it.size }
    }

    var showListSelector by remember { mutableStateOf(false) }

    val blurRadius by animateDpAsState(
        targetValue = if (showWelcomeDialog) 10.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundBlur"
    )

    Box {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurRadius)
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

                                AnimatedVisibility(
                                    visible = true,
                                    enter =
                                        slideInVertically(
                                            initialOffsetY = { it },
                                            animationSpec = tween(
                                                durationMillis = 350,
                                                easing = FastOutSlowInEasing
                                            )
                                        ) + fadeIn(),
                                    exit = fadeOut()
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
                        when (uiState.toScreen()) {
                            Screen.StoreSelection -> "Supermarkt wählen"
                            else -> "ShopMe"
                        }

                    CenterAlignedTopAppBar(

                        title = {

                            AnimatedContent(
                                targetState = uiState,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "TopBarAnimation"
                            ) {

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

                                                Image(
                                                    painter = painterResource(id = store.logoRes),
                                                    contentDescription = store.displayName,
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
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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

                            onClick = { vm.dispatch(
                                ShoppingAction.StartMultiStoreCreation
                            ) },

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
                                .padding(bottom = 12.dp)
                                .shadow(12.dp, RoundedCornerShape(24.dp)),

                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 14.dp
                            )

                        )

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
                                lists = userLists,
                                activeListId = activeList?.id,
                                onEdit = { vm.editList(it) },
                                onDelete = { vm.deleteList(it) },
                                onCreateNewList = {
                                    vm.dispatch(
                                        ShoppingAction.StartMultiStoreCreation
                                    )
                                }
                            )

                        }

                        Screen.Items -> {

                            ShoppingContent(
                                vm = vm,
                                speechController = speechController
                            )

                        }

                        Screen.StoreSelection -> {

                            val selectedStores =
                                (uiState as ShoppingUiState.MultiSelect).selectedStores

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

            WelcomeDialog(
                onCreateFirstList = {
                    vm.dismissWelcomeDialog()
                    vm.dispatch(
                        ShoppingAction.StartMultiStoreCreation
                    )
                }
            )

        }

    }

}