package de.shopme.presentation.effect

import android.content.Context
import android.content.Intent
import de.shopme.app.MainActivity
import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.data.sync.logging.SyncLog
import de.shopme.data.sync.logging.UILog
import de.shopme.domain.auth.AuthProvider
import de.shopme.domain.item.ItemActionHandler
import de.shopme.presentation.event.ShopEvent
import de.shopme.presentation.viewmodel.ShoppingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class ShoppingEffectHandler(
    private val authProvider: AuthProvider,
    private val viewModel: ShoppingViewModel,
    private val scope: CoroutineScope,
    private val itemActionHandler: ItemActionHandler,
    private val firestoreGateway: FirestoreGateway,
    private val appContext: Context
) {

    fun handle(effect: UIEffect) {

        when (effect) {

            is UIEffect.StartGoogleSignIn -> {

                val activity = appContext as MainActivity

                activity.startGoogleLogin()
            }

            is UIEffect.DeleteAllLists -> {

                scope.launch {

                    viewModel.dispatch(
                        event = ShopEvent.List.StartDeleteAll
                    )

                    val startTime = System.currentTimeMillis()
                    val minDuration = 1200L

                    try {

                        viewModel.deleteAllLists()

                    } finally {

                        val elapsed =
                            System.currentTimeMillis() - startTime

                        val remaining =
                            minDuration - elapsed

                        if (remaining > 0) {
                            kotlinx.coroutines.delay(remaining)
                        }

                        viewModel.dispatch(
                            event = ShopEvent.List.FinishDeleteAll
                        )

                        viewModel.onDeleteAllCompleted()
                    }
                }
            }

            is UIEffect.CreateLists -> {

                RuntimeLog.runtime(
                    "CreateLists effect received"
                )

                viewModel.dispatch(
                    event = ShopEvent.List.StartSorting
                )

                scope.launch {

                    val startTime = System.currentTimeMillis()
                    val minDuration = 1200L

                    try {

                        viewModel.createListsWithSorting(
                            stores = effect.stores,
                            customLists = effect.customLists
                        )

                    } finally {

                        val elapsed =
                            System.currentTimeMillis() - startTime

                        val remaining =
                            minDuration - elapsed

                        if (remaining > 0) {
                            kotlinx.coroutines.delay(remaining)
                        }

                        viewModel.dispatch(
                            event = ShopEvent.List.FinishSorting
                        )
                    }
                }
            }

            is UIEffect.AddItem -> {
                scope.launch {
                    val listId = viewModel.getCurrentListId() ?: return@launch
                    itemActionHandler.addItem(effect.name, listId)
                }
            }

            is UIEffect.UpdateItem -> {
                scope.launch {
                    itemActionHandler.updateItem(effect.item, effect.newName)
                }
            }

            is UIEffect.DeleteItem -> {
                scope.launch {
                    itemActionHandler.deleteItem(effect.item)
                }
            }

            is UIEffect.ToggleItem -> {
                scope.launch {
                    itemActionHandler.updateItemChecked(
                        itemId = effect.itemId,
                        newChecked = effect.newChecked
                    )
                }
            }

            is UIEffect.LoadUserProfile -> {
                scope.launch {
                    viewModel.performLoadUserProfile(effect)
                }
            }

            is UIEffect.UpdateUserProfile -> {
                scope.launch {
                    viewModel.performUpdateUserProfile(effect)
                }
            }

            is UIEffect.DeleteAccount -> {
                scope.launch {

                    val userId = authProvider.getCurrentUserUidOrNull() ?: return@launch

                    viewModel.performDeleteAccountFlow(
                        userId = userId,
                        getIdToken = { null }
                    )
                }
            }

            is UIEffect.UnlinkGoogle -> {
                scope.launch {
                    viewModel.performUnlinkGoogle()
                }
            }

            is UIEffect.StartShareIntent -> {
                startShareIntent(effect.listId)
            }

            is UIEffect.ShareList -> {
                scope.launch {

                    try {
                        UILog.navigation(
                            "Share intent launched | listId=${effect.listId}"
                        )

                        val ownerId = authProvider.currentUserId()
                            ?: throw IllegalStateException("User not authenticated")

                        val createdByName = authProvider.getDisplayName()
                            ?: "Unbekannt"

                        val inviteLink = firestoreGateway.createInviteLink(
                            listId = effect.listId,
                            createdByName = createdByName,
                            ownerId = ownerId
                        )

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, inviteLink)
                        }

                        // 🔥 WICHTIG: FLAG AUF DEM CHOOSER
                        val chooser = Intent.createChooser(intent, "Liste teilen").apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }

                        appContext.startActivity(chooser)

                        // 🔥 danach Erfolg
                        viewModel.dispatch(event = ShopEvent.List.ShareStarted)

                    } catch (e: Exception) {
                        SyncLog.recovery(
                            "Share flow failed",
                            e
                        )
                    }
                }
            }

            else -> {
                RuntimeLog.runtimeError(
                    "Unhandled UIEffect: $effect"
                )
            }
        }
    }

    private fun startShareIntent(listId: String) {

        scope.launch {

            try {

                UILog.navigation(
                    "Share intent started | listId=$listId"
                )

                // 🔥 NEU: State holen
                val state = viewModel.state.value

                val createdByName = state.displayName ?: "Unbekannt"

                val ownerId = authProvider.getUserId()
                    ?: throw Exception("User not authenticated")

                // 🔥 FIX: neue Signatur verwenden
                val inviteLink = firestoreGateway.createInviteLink(
                    listId = listId,
                    createdByName = createdByName,
                    ownerId = ownerId
                )

                UILog.navigation(
                    "Invite link created | listId=$listId"
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, inviteLink)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val chooser = Intent.createChooser(intent, "Liste teilen").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                appContext.startActivity(chooser)

            } catch (e: Exception) {
                SyncLog.recovery(
                    "Share intent failed | listId=$listId | error=${e.message}"
                )
            }
        }
    }
}