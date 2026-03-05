package de.shopme

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import de.shopme.data.FirestoreShoppingRepository
import de.shopme.data.auth.FirebaseAuthProvider
import de.shopme.domain.auth.AuthProvider
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.speech.SpeechController
import de.shopme.ui.ShopEvent
import de.shopme.ui.ShopMeApp
import de.shopme.ui.theme.ShopMeTheme
import de.shopme.util.*

class MainActivity : ComponentActivity() {

    private val authProvider: AuthProvider = FirebaseAuthProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("ACTIVITY", "onCreate called")

        val deepLinkData: Uri? = intent?.data

        setContent {
            ShopMeTheme {

                val context = this@MainActivity

                val repository = remember {
                    FirestoreShoppingRepository()
                }

                val quantityMapper = remember {
                    QuantityMapper(loadJsonMap(context, "quantity_mapping.json"))
                }

                val categoryMapper = remember {
                    CategoryMapper(loadCategoryConfig(context, "category_mapping.json"))
                }

                val networkMonitor = remember {
                    NetworkMonitor(context)
                }

                val factory = remember {
                    androidx.lifecycle.viewmodel.viewModelFactory {
                        initializer {
                            ShoppingViewModel(
                                repository = repository,
                                quantityMapper = quantityMapper,
                                categoryMapper = categoryMapper,
                                networkMonitor = networkMonitor,
                                authProvider = authProvider
                            )
                        }
                    }
                }

                val vm: ShoppingViewModel = viewModel(factory = factory)

                // Bootstrap exakt einmal
                LaunchedEffect(Unit) {
                    Log.d("BOOTSTRAP", "Calling bootstrap()")

                    val listId = deepLinkData?.getQueryParameter("listId")
                    val inviteId = deepLinkData?.getQueryParameter("inviteId")

                    vm.bootstrap(
                        deepLinkListId = listId,
                        deepLinkInviteId = inviteId
                    )
                }

                val speechController = remember {
                    SpeechController(context) {
                        vm.onEvent(ShopEvent.AddItem(it))
                    }
                }

                // 🔥 HIER ist deine echte UI
                ShopMeApp(
                    vm = vm,
                    speechController = speechController
                )
            }
        }
    }
}