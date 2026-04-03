package de.shopme.app

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
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
import com.google.firebase.firestore.FirebaseFirestore
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
import de.shopme.data.remote.MembershipListener
import de.shopme.ui.app.ShopMeApp
import de.shopme.ui.theme.ShopMeTheme
import kotlinx.coroutines.delay
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

        val uri: Uri? = intent?.data

        Log.d("DEEPLINK", "RAW URI → $uri")

        val listId = uri?.getQueryParameter("listId")
        val inviteId = uri?.getQueryParameter("inviteId")

        Log.d("DEEPLINK", "listId=$listId inviteId=$inviteId")




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

                val membershipListener = remember {
                    MembershipListener(
                        firestore = FirebaseFirestore.getInstance(),
                        syncCoordinator = syncCoordinator
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
                                authProvider = authProvider,
                                speechItemParser = speechParser,
                                firestoreDataSource = firestoreDataSource,
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

                val auth = FirebaseAuth.getInstance()
                var bootstrapped by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {

                    if (bootstrapped) return@LaunchedEffect

                    bootstrapped = true

                    try {

                        Log.d("BOOT", "START BOOTSTRAP")

                        val user = auth.currentUser?.let { current ->

                            Log.d("BOOT", "TRY EXISTING USER → ${current.uid}")

                            current.getIdToken(true).await()

                            current

                        } ?: run {

                            Log.w("BOOT", "USER INVALID → RECREATE")

                            auth.signOut()

                            val result = auth.signInAnonymously().await()

                            Log.d("BOOT", "ANON LOGIN SUCCESS")

                            result.user ?: throw IllegalStateException("User null after recreate")
                        }

                        Log.d("BOOT", "USER READY → ${user.uid}")

                        syncCoordinator.start()
                        membershipListener.start(user.uid)

                        // 🔥 EINZIGER Bootstrap-Aufruf (hier!)
                        vm.bootstrap(
                            deepLinkListId = listId,
                            deepLinkInviteId = inviteId
                        )

                        // 🔥 KEIN zweiter Token Call mehr!

                        val uri: Uri? = intent?.data
                        val listId = uri?.getQueryParameter("listId")
                        val inviteId = uri?.getQueryParameter("inviteId")

                    } catch (e: Exception) {
                        Log.e("BOOT", "BOOT FAILED", e)
                    }
                }

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