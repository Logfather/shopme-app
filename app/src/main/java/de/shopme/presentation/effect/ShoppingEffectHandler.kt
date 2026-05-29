package de.shopme.presentation.effect

import android.content.Context
import android.content.Intent
import android.util.Log
import de.shopme.data.datasource.firestore.FirestoreGateway
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

        Log.d(
            "EFFECT_HANDLER",
            "HANDLER instance=${this.hashCode()} effect=$effect"
        )

        when (effect) {

            is UIEffect.AddItem -> {
                Log.d("EFFECT_DEBUG", "AddItem effect: ${effect.name}")

                scope.launch {
                    val listId = viewModel.currentListId.value ?: return@launch
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
                        Log.d("SHARE_FLOW", "Start share for list=${effect.listId}")

                        val ownerId = authProvider.currentUserId()
                            ?: throw IllegalStateException("User not authenticated")

                        val createdByName = authProvider.getDisplayName()
                            ?: "Unbekannt"

                        val inviteLink = firestoreGateway.createInviteLink(
                            listId = effect.listId,
                            createdByName = createdByName,
                            ownerId = ownerId
                        )

                        Log.d("SHARE_FLOW", "Invite created: $inviteLink")

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
                        Log.e("SHARE_FLOW", "Share failed listId=${effect.listId}", e)
                    }
                }
            }

            else -> {
                Log.w("UI_EFFECT", "Unhandled effect: $effect")
            }
        }
    }

    private fun startShareIntent(listId: String) {

        scope.launch {

            try {

                Log.d("SHARE_FLOW", "Start share for list=$listId")

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

                Log.d("SHARE_FLOW", "Invite created: $inviteLink")

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
                Log.e("SHARE_FLOW", "Share failed", e)
            }
        }
    }
}