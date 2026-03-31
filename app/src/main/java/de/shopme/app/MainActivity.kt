package de.shopme.app

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import de.shopme.core.AppScope
import de.shopme.core.json.loadJsonMap
import de.shopme.core.network.NetworkMonitor
import de.shopme.data.auth.FirebaseAuthProvider
import de.shopme.data.datasource.catalog.CatalogLoader
import de.shopme.data.datasource.firestore.FirestoreDataSource
import de.shopme.data.datasource.room.ShopMeDatabase
import de.shopme.data.input.speech.SpeechController
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.ConflictResolver
import de.shopme.data.sync.FirestoreListener
import de.shopme.data.sync.SyncCoordinator
import de.shopme.domain.auth.AuthProvider
import de.shopme.domain.catalog.CatalogIndex
import de.shopme.domain.service.*
import de.shopme.domain.usecase.CreateListUseCase
import de.shopme.domain.usecase.DeleteListUseCase
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.R
import de.shopme.ui.app.ShopMeApp
import de.shopme.ui.theme.ShopMeTheme
import kotlinx.coroutines.launch

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
                        Log.e("AUTH", "ID TOKEN NULL")
                    }

                } catch (e: Exception) {
                    Log.e("AUTH", "Google sign-in failed", e)
                }
            }
        }

    private lateinit var shoppingViewModel: ShoppingViewModel

    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(getString(de.shopme.R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso)

        // Pending Invite speichern
        intent?.data?.let { uri ->
            if (uri.host == "shopme-app.de" && uri.path?.contains("invite") == true) {
                val listId = uri.getQueryParameter("listId")
                if (listId != null) {
                    getSharedPreferences("shopme", MODE_PRIVATE)
                        .edit()
                        .putString("pending_invite_list_id", listId)
                        .apply()
                }
            }
        }

        setContent {

            ShopMeTheme {

                val activityContext = this@MainActivity

                val database = remember {
                    Room.databaseBuilder(
                        activityContext,
                        ShopMeDatabase::class.java,
                        "shopme_database"
                    )
                        .addMigrations(
                            ShopMeDatabase.MIGRATION_4_5
                        )
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {}
                        })
                        .build()
                }

                val listDao = remember { database.listDao() }
                val itemDao = remember { database.itemDao() }
                val changeQueueDao = remember { database.changeQueueDao() }

                val roomRepository = remember {
                    RoomShoppingRepository(
                        itemDao = itemDao,
                        listDao = listDao,
                        changeQueueDao = changeQueueDao
                    )
                }

                val appScope = remember { AppScope() }

                val catalogService = remember {
                    val loader = CatalogLoader(activityContext)
                    val items = loader.load()
                    val index = CatalogIndex(items)
                    CatalogService(index)
                }

                val speechParser = remember {
                    SpeechItemParser(catalogService)
                }

                val firestoreDataSource = remember { FirestoreDataSource() }
                val conflictResolver = remember { ConflictResolver() }

                val firestoreListener = remember {
                    FirestoreListener(
                        dataSource = firestoreDataSource,
                        itemDao = itemDao,
                        listDao = listDao,
                        conflictResolver = conflictResolver,
                        appScope = appScope
                    )
                }

                val syncCoordinator = remember {
                    SyncCoordinator(
                        changeQueueDao = changeQueueDao,
                        itemDao = itemDao,
                        listDao = listDao,
                        firestore = firestoreDataSource,
                        appScope = appScope
                    )
                }

                val quantityMapper = remember {
                    QuantityMapper(
                        loadJsonMap(activityContext, "quantity_mapping.json")
                    )
                }

                val categoryMapper = remember {
                    CategoryMapper(catalogService.index)
                }

                val networkMonitor = remember {
                    NetworkMonitor(activityContext)
                }

                val factory = remember {

                    val createListUseCase = CreateListUseCase(roomRepository)
                    val deleteListUseCase =
                        DeleteListUseCase(roomRepository, firestoreDataSource)

                    viewModelFactory {
                        initializer {
                            ShoppingViewModel(
                                createListUseCase = createListUseCase,
                                deleteListUseCase = deleteListUseCase,
                                roomRepository = roomRepository,
                                quantityMapper = quantityMapper,
                                categoryMapper = categoryMapper,
                                networkMonitor = networkMonitor,
                                authProvider = authProvider,
                                speechItemParser = speechParser,
                                firestoreDataSource = firestoreDataSource,
                                itemDao = itemDao,
                                listDao = listDao,
                                firestoreListener = firestoreListener
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

                    Log.e("BOOT", "LaunchedEffect STARTED")

                    val auth = FirebaseAuth.getInstance()

                    fun runBootstrap() {

                        Log.e("BOOT", "runBootstrap CALLED")

                        val user = auth.currentUser

                        Log.e("BOOT", "USER = $user")

                        if (user == null) {
                            Log.e("BOOT", "USER IS NULL → ABORT")
                            return
                        }

                        Log.e("BOOT", "STARTING SYNC")

                        syncCoordinator.start()

                        Log.e("BOOT", "SYNC START CALLED")

                        val uri: Uri? = intent?.data
                        val listId = uri?.getQueryParameter("listId")
                        val inviteId = uri?.getQueryParameter("inviteId")

                        vm.bootstrap(
                            deepLinkListId = listId,
                            deepLinkInviteId = inviteId
                        )

                        Log.e("BOOT", "BOOTSTRAP CALLED")

                        if (inviteId != null) {
                            vm.joinViaInvite(inviteId)
                        }
                    }

                    if (auth.currentUser != null) {
                        Log.e("BOOT", "USER EXISTS → DIRECT BOOTSTRAP")
                        runBootstrap()
                        return@LaunchedEffect
                    }

                    Log.e("BOOT", "NO USER → SIGN IN")

                    auth.signInAnonymously()
                        .addOnSuccessListener {
                            Log.e("BOOT", "ANON LOGIN SUCCESS")
                            runBootstrap()
                        }
                        .addOnFailureListener {
                            Log.e("BOOT", "ANON LOGIN FAILED", it)
                        }
                }

//                LaunchedEffect(Unit) {
//
//
//                    val auth = FirebaseAuth.getInstance()
//
//                    fun runBootstrap() {
//
//                        val user = auth.currentUser ?: return
//
//                        val uri: Uri? = intent?.data
//                        val listId = uri?.getQueryParameter("listId")
//                        val inviteId = uri?.getQueryParameter("inviteId")
//
//                        vm.bootstrap(
//                            deepLinkListId = listId,
//                            deepLinkInviteId = inviteId
//                        )
//
//                        if (inviteId != null) {
//                            vm.joinViaInvite(inviteId)
//                        }
//                    }
//
//                    // 🔥 Fall 1: User existiert
//                    if (auth.currentUser != null) {
//                        runBootstrap()
//                        return@LaunchedEffect
//                    }
//
//                    // 🔥 Fall 2: Kein User → Login + danach Bootstrap
//
//                    try {
//                        auth.signInAnonymously()
//                            .addOnSuccessListener {
//                                runBootstrap() // 🔥 HIER IST DER FIX
//                            }
//                            .addOnFailureListener {
//                            }
//                    } catch (e: Exception) {
//                    }
//                }

                val speechController = remember {
                    SpeechController(
                        context = activityContext,
                        catalogService = catalogService
                    )
                }

                ShopMeApp(
                    vm = vm,
                    speechController = speechController,
                    catalogService = catalogService
                )
            }
        }
    }

    private fun onGoogleIdTokenReceived(idToken: String) {

        Log.d("AUTH", "ID TOKEN RECEIVED: ${idToken.take(10)}...")

        val beforeUid = com.google.firebase.auth.FirebaseAuth.getInstance().uid
        Log.d("AUTH", "UID BEFORE LINK: $beforeUid")

        lifecycleScope.launch {

            val result = shoppingViewModel.linkWithGoogle(idToken)

            val afterUid = com.google.firebase.auth.FirebaseAuth.getInstance().uid
            Log.d("AUTH", "UID AFTER LINK: $afterUid")

            if (result.isSuccess) {
                Log.d("AUTH", "Google account linked SUCCESS")
            } else {
                Log.e("AUTH", "Link FAILED", result.exceptionOrNull())
            }
        }
    }

    fun startGoogleLogin() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }
}