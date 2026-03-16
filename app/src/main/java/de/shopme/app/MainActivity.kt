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
import de.shopme.core.json.loadJsonMap
import de.shopme.core.network.NetworkMonitor
import de.shopme.data.auth.FirebaseAuthProvider
import de.shopme.data.input.speech.SpeechController
import de.shopme.data.repository.FirestoreShoppingRepository
import de.shopme.domain.auth.AuthProvider
import de.shopme.domain.service.CategoryMapper
import de.shopme.domain.service.QuantityMapper
import de.shopme.domain.usecase.CreateListUseCase
import de.shopme.domain.usecase.DeleteListUseCase
import de.shopme.domain.usecase.SetActiveListUseCase
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.ui.app.ShopMeApp
import de.shopme.ui.theme.ShopMeTheme

import de.shopme.data.datasource.catalog.CatalogLoader
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

                val repository = remember {
                    FirestoreShoppingRepository()
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

                    val createListUseCase = CreateListUseCase(repository)
                    val deleteListUseCase = DeleteListUseCase(repository)
                    val setActiveListUseCase = SetActiveListUseCase(repository)

                    viewModelFactory {

                        initializer {

                            ShoppingViewModel(
                                createListUseCase = createListUseCase,
                                deleteListUseCase = deleteListUseCase,
                                setActiveListUseCase = setActiveListUseCase,
                                repository = repository,
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

                    vm.bootstrap(
                        deepLinkListId = listId,
                        deepLinkInviteId = inviteId
                    )
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
