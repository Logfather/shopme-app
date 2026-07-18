package de.shopme.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import de.shopme.auth.FirebaseAuthProvider
import de.shopme.core.network.NetworkMonitor
import de.shopme.core.sound.SoundPlayer
import de.shopme.data.datasource.catalog.CatalogLoader
import de.shopme.data.input.speech.SpeechController
import de.shopme.data.input.speech.SpeechItemParser
import de.shopme.data.sync.logging.NetworkLog
import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.data.sync.queue.ChangeQueue
import de.shopme.domain.account.AccountDeletionManager
import de.shopme.domain.auth.AuthProvider
import de.shopme.domain.catalog.CatalogIndex
import de.shopme.domain.service.CatalogService
import de.shopme.domain.service.CategoryMapper
import de.shopme.domain.service.QuantityMapper
import de.shopme.domain.usecase.DeleteListUseCase
import de.shopme.presentation.viewmodel.AuthViewModel
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.ui.app.HivraApp
import de.shopme.ui.theme.HivraTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    private val authProvider: AuthProvider = FirebaseAuthProvider()

    private val googleSignInLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {

                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

                try {
                    val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)

                    val idToken = account.idToken

                    if (idToken != null) {
                        onGoogleIdTokenReceived(idToken)
                    } else {
                        RuntimeLog.appStartError(
                            "Google sign-in failed | id token null"
                        )
                    }

                } catch (e: Exception) {
                    RuntimeLog.appStartError(
                        "Google sign-in failed",
                        e
                    )
                }
            }
        }

    private var shoppingViewModel: ShoppingViewModel? = null

    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val runtime = (application as HivraApplication).runtime

        val productionNutritionPipeline = runtime.productionNutritionPipeline

        val nutritionInsightService =
            runtime.nutritionInsightService

        val pendingInviteStore =
            runtime.pendingInviteStore

        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(getString(de.shopme.R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso)
        SoundPlayer.init(this)


        // Pending Invite speichern
        intent?.data?.let { uri ->

            if (
                uri.host == "shopme-app.de" &&
                uri.path?.contains("invite") == true
            ) {

                val listId =
                    uri.getQueryParameter("listId")

                val inviteId =
                    uri.getQueryParameter("inviteId")

                if (
                    listId != null &&
                    inviteId != null
                ) {

                    pendingInviteStore
                        .savePendingInvite(
                            listId = listId,
                            inviteId = inviteId
                        )
                }
            }
        }

        setContent {

            val activityContext = this@MainActivity
            val firestoreDataSource = runtime.firestoreGateway
            val listDao = runtime.listDao
            val itemDao = runtime.itemDao
            val changeQueueDao = runtime.changeQueueDao
            val conflictResolver = runtime.conflictResolver
            val roomRepository = runtime.roomRepository
            val syncCoordinator = runtime.syncCoordinator


            val catalogService = remember {

                val loader = CatalogLoader(activityContext)

                val items = loader.load()

                val index = CatalogIndex(items)

                CatalogService(index)

            }

            LaunchedEffect(Unit) {

                listOf(
                    "Milch",
                    "Butter",
                    "Banane",
                    "Nutella",
                    "Coca Cola"
                ).forEach { name ->

                    val item =
                        catalogService.resolveExactSpeech(name)

                }
            }

            val authViewModel = remember { AuthViewModel(authProvider) }

            val authUser by authViewModel.authUser.collectAsState()

            LaunchedEffect(authUser?.uid) {

                val uid = authUser?.uid

                if (uid != null) {

                    runtime.startUserSync(uid)

                } else {

                    runtime.stopUserSync()
                }
            }

            val quantityMapper = remember {
                QuantityMapper(
                    emptyMap()
                )
            }

            val categoryMapper = remember {
                CategoryMapper(catalogService.index)
            }

            val networkMonitor = remember {
                NetworkMonitor(activityContext)
            }

            LaunchedEffect(Unit) {

                networkMonitor
                    .observe()
                    .distinctUntilChanged()
                    .drop(1)
                    .collect { connected ->

                        NetworkLog.monitor(
                            "connected=$connected"
                        )

                        if (connected) {

                            runtime
                                .syncRuntimeOrchestrator
                                .onReconnect()
                        }
                    }
            }

            val factory = remember {

                val deleteListUseCase =
                    DeleteListUseCase(roomRepository, firestoreDataSource)
                val syncCoordinatorRef = syncCoordinator
                val inMemoryChangeQueue = ChangeQueue()
                val speechItemParser = SpeechItemParser(catalogService)
                val authViewModelRef = authViewModel
                val accountDeletionManager = AccountDeletionManager(
                    syncCoordinator = syncCoordinatorRef,
                    listDao = listDao,
                    changeQueueDao = changeQueueDao,
                    firestore = firestoreDataSource,
                    authViewModel = authViewModelRef,
                    authProvider = authProvider
                )

                viewModelFactory {
                    initializer {
                        ShoppingViewModel(
                            deleteListUseCase = deleteListUseCase,
                            roomRepository = roomRepository,
                            quantityMapper = quantityMapper,
                            categoryMapper = categoryMapper,
                            authProvider = authProvider,
                            firestoreDataSource = firestoreDataSource,
                            listDao = listDao,
                            changeQueue = inMemoryChangeQueue,
                            authViewModel = authViewModel,
                            accountDeletionManager = accountDeletionManager,
                            appContext = this@MainActivity.applicationContext,
                            speechItemParser = speechItemParser
                        )
                    }
                }
            }



            // ------------------------------------------------------------
            // BOOTSTRAP
            // ------------------------------------------------------------

            val vm: ShoppingViewModel = viewModel(factory = factory)

            shoppingViewModel = vm


            LaunchedEffect(Unit) {
                vm.syncUserFromFirebase()
            }

            LaunchedEffect(Unit) {

                vm.shareEvent.collect { link ->

                    val intent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, link)
                    }

                    val chooser = android.content.Intent.createChooser(
                        intent,
                        "Liste teilen"
                    )

                    startActivity(chooser)
                }
            }

            val auth = FirebaseAuth.getInstance()
            var bootstrapped by remember { mutableStateOf(false) }

            LaunchedEffect(intent?.data) {

                if (bootstrapped) return@LaunchedEffect

                bootstrapped = true

                // ============================================================
                // ✅ LOCAL BOOTSTRAP IMMER SOFORT
                // ============================================================

                val uri: Uri? = intent?.data
                val listId = uri?.getQueryParameter("listId")
                val inviteId = uri?.getQueryParameter("inviteId")

                vm.bootstrap(
                    deepLinkListId = listId,
                    deepLinkInviteId = inviteId
                )

                // ============================================================
                // ✅ REMOTE AUTH SEPARAT
                // ============================================================

                lifecycleScope.launch {

                    try {

                        val user = run {

                            val current =
                                auth.currentUser

                            if (current == null) {

                                RuntimeLog.runtime(
                                    "No Firebase user -> creating anonymous user"
                                )

                                val result =
                                    auth.signInAnonymously().await()

                                result.user
                                    ?: throw IllegalStateException(
                                        "User null after anonymous sign-in"
                                    )

                            } else {

                                try {

                                    current.getIdToken(true).await()

                                    RuntimeLog.runtime(
                                        "Firebase user validated uid=${current.uid}"
                                    )

                                    current

                                } catch (e: Exception) {

                                    RuntimeLog.recoveryError(
                                        "User token validation failed -> recreate",
                                        e
                                    )

                                    auth.signOut()

                                    val result =
                                        auth.signInAnonymously().await()

                                    result.user
                                        ?: throw IllegalStateException(
                                            "User null after recreate"
                                        )
                                }
                            }
                        }

                        RuntimeLog.runtime(
                            "Auth bootstrap complete uid=${user.uid}"
                        )

                        vm.syncUserFromFirebase()

                    } catch (e: Exception) {

                        RuntimeLog.runtimeError(
                            "Auth bootstrap failed",
                            e
                        )
                    }
                }
            }

            val speechController = remember {
                SpeechController(
                    context = activityContext,
                    catalogService = catalogService
                )
            }

            HivraTheme {

                HivraApp(

                    vm = vm,

                    appServices = runtime.appServices,

                    speechController = speechController,

                    catalogService = catalogService,

                    productionNutritionPipeline =
                        productionNutritionPipeline,

                    nutritionInsightService =
                        nutritionInsightService

                )
            }
        }
    }

    private fun onGoogleIdTokenReceived(idToken: String) {

        lifecycleScope.launch {

            val vm = shoppingViewModel
                ?: run {
                    RuntimeLog.runtimeError(
                        "ViewModel not ready -> abort Google flow"
                    )
                    return@launch
                }

            val result = vm.linkWithGoogle(idToken)

            if (result.isSuccess) {

                vm.onGoogleSignInSuccess()

                vm.bootstrap()

                vm.loadUserProfile()

            } else {

                RuntimeLog.runtimeError(
                    "Google link failed",
                    result.exceptionOrNull()
                )
            }
        }
    }

    fun startGoogleLogin() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    override fun onResume() {
        super.onResume()

        shoppingViewModel?.notifyReturnedFromShare()
    }
}