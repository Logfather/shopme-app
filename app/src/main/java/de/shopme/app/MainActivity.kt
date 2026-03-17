package de.shopme.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import de.shopme.core.json.loadJsonMap
import de.shopme.core.network.NetworkMonitor
import de.shopme.data.auth.FirebaseAuthProvider
import de.shopme.data.input.speech.SpeechController
import de.shopme.domain.auth.AuthProvider
import de.shopme.domain.service.CategoryMapper
import de.shopme.domain.service.QuantityMapper
import de.shopme.domain.usecase.CreateListUseCase
import de.shopme.domain.usecase.DeleteListUseCase
import de.shopme.data.datasource.room.ShopMeDatabase
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.ui.app.ShopMeApp
import de.shopme.ui.theme.ShopMeTheme

import de.shopme.data.datasource.catalog.CatalogLoader
import de.shopme.data.datasource.firestore.FirestoreDataSource
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.ConflictResolver
import de.shopme.data.sync.FirestoreListener
import de.shopme.data.sync.SyncCoordinator
import de.shopme.domain.catalog.CatalogIndex
import de.shopme.domain.service.CatalogService
import de.shopme.domain.service.SpeechItemParser


class MainActivity : ComponentActivity() {

    private val authProvider: AuthProvider = FirebaseAuthProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deepLinkData: Uri? = intent?.data

        setContent {
            ShopMeTheme {

                val context = this@MainActivity

                // ------------------------------------------------------------
                // Local Database (Room)
                // ------------------------------------------------------------

                val database = remember {

                    Room.databaseBuilder(
                        context,
                        ShopMeDatabase::class.java,
                        "shopme_database"
                    )
                        .fallbackToDestructiveMigration()   // ✅ HIER
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

                // ------------------------------------------------------------
                // Catalog (einmalig erzeugen)
                // ------------------------------------------------------------

                val catalogService = remember {

                    val loader = CatalogLoader(context)
                    val items = loader.load()

                    val index = CatalogIndex(items)

                    CatalogService(index)
                }

                val speechParser = remember {
                    SpeechItemParser(catalogService)
                }

                // ------------------------------------------------------------
                // Repository + Mapper
                // ------------------------------------------------------------

                val firestoreDataSource = remember {
                    FirestoreDataSource()
                }

                val conflictResolver = remember {
                    ConflictResolver()
                }

                val firestoreListener = remember {
                    FirestoreListener(
                        dataSource = firestoreDataSource,
                        itemDao = itemDao,
                        listDao = listDao,
                        conflictResolver = conflictResolver
                    )
                }

                val syncCoordinator = remember {
                    SyncCoordinator(
                        changeQueueDao = changeQueueDao,
                        itemDao = itemDao,
                        firestore = firestoreDataSource
                    )
                }

                val quantityMapper = remember {
                    QuantityMapper(
                        loadJsonMap(context, "quantity_mapping.json")
                    )
                }

                val categoryMapper = remember {
                    CategoryMapper(catalogService.index)
                }

                val networkMonitor = remember {
                    NetworkMonitor(context)
                }

                // ------------------------------------------------------------
                // ViewModel Factory
                // ------------------------------------------------------------

                val factory = remember {

                    val createListUseCase = CreateListUseCase(roomRepository)
                    val deleteListUseCase = DeleteListUseCase(roomRepository)

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
                                speechItemParser = speechParser
                            )
                        }
                    }
                }

                val vm: ShoppingViewModel = viewModel(factory = factory)

                // ------------------------------------------------------------
                // Bootstrap (DeepLink Handling)
                // ------------------------------------------------------------

                LaunchedEffect(Unit) {

                    val listId = deepLinkData?.getQueryParameter("listId")
                    val inviteId = deepLinkData?.getQueryParameter("inviteId")

                    syncCoordinator.start {
                        vm.currentListId.value
                    }

                    vm.bootstrap(
                        deepLinkListId = listId,
                        deepLinkInviteId = inviteId
                    )

                    val uid = authProvider.getCurrentUserId() ?: return@LaunchedEffect

                    firestoreListener.startListSync(uid)

                    listId?.let {
                        firestoreListener.startItemSync(it)
                    }
                }

                // ------------------------------------------------------------
                // Speech Controller
                // ------------------------------------------------------------

                val speechController = remember {
                    SpeechController(
                        context = context,
                        catalogService = catalogService
                    )
                }

                // ------------------------------------------------------------
                // UI
                // ------------------------------------------------------------

                ShopMeApp(
                    vm = vm,
                    speechController = speechController,
                    catalogService = catalogService
                )
            }
        }
    }
}
