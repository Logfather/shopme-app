package de.shopme.app

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import de.shopme.ui.app.ShopMeApp
import de.shopme.ui.theme.ShopMeTheme

class MainActivity : ComponentActivity() {

    private val authProvider: AuthProvider = FirebaseAuthProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e("BOOTSTRAP", "ACTIVITY STARTED")

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
            Log.e("BOOTSTRAP", "SETCONTENT STARTED")
            ShopMeTheme {

                val activityContext = this@MainActivity

                val database = remember {
                    Room.databaseBuilder(
                        activityContext,
                        ShopMeDatabase::class.java,
                        "shopme_database"
                    )
                        .addMigrations(ShopMeDatabase.MIGRATION_3_4)
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
                // 🔥 FINALER BOOTSTRAP (sofort + fallback)
                // ------------------------------------------------------------

                val vm: ShoppingViewModel = viewModel(factory = factory)

                LaunchedEffect(Unit) {


                    val auth = FirebaseAuth.getInstance()

                    fun runBootstrap() {

                        val user = auth.currentUser ?: return

                        Log.d("BOOTSTRAP", "Auth READY → ${user.uid}")

                        val uri: Uri? = intent?.data
                        val listId = uri?.getQueryParameter("listId")
                        val inviteId = uri?.getQueryParameter("inviteId")

                        vm.bootstrap(
                            deepLinkListId = listId,
                            deepLinkInviteId = inviteId
                        )

                        if (inviteId != null) {
                            vm.joinViaInvite(inviteId)
                        }

                        Log.d("BOOTSTRAP", "Bootstrap COMPLETE")
                    }

                    // 🔥 Fall 1: User existiert
                    if (auth.currentUser != null) {
                        runBootstrap()
                        return@LaunchedEffect
                    }

                    // 🔥 Fall 2: Kein User → Login + danach Bootstrap
                    Log.d("BOOTSTRAP", "No user → signing in anonymously")

                    try {
                        auth.signInAnonymously()
                            .addOnSuccessListener {
                                Log.d("BOOTSTRAP", "Anonymous login SUCCESS")
                                runBootstrap() // 🔥 HIER IST DER FIX
                            }
                            .addOnFailureListener {
                                Log.e("BOOTSTRAP", "Anonymous login FAILED", it)
                            }
                    } catch (e: Exception) {
                        Log.e("BOOTSTRAP", "Auth crash", e)
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
}