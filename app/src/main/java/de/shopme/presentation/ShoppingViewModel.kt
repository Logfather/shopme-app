package de.shopme.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import de.shopme.data.FirestoreShoppingRepository
import de.shopme.data.ShoppingItemEntity
import de.shopme.domain.auth.AuthProvider
import de.shopme.ui.ShopEvent
import de.shopme.util.CategoryMapper
import de.shopme.util.NetworkMonitor
import de.shopme.util.QuantityMapper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ShoppingViewModel(
    private val repository: FirestoreShoppingRepository,
    private val quantityMapper: QuantityMapper,
    private val categoryMapper: CategoryMapper,
    private val networkMonitor: NetworkMonitor,
    private val authProvider: AuthProvider
) : ViewModel() {

    private val _uiItems = MutableStateFlow<List<ShoppingItemEntity>>(emptyList())
    val uiItems: StateFlow<List<ShoppingItemEntity>> = _uiItems.asStateFlow()

    val groupedItems: StateFlow<Map<String, List<ShoppingItemEntity>>> =
        uiItems
            .map { items ->
                items
                    .filter { it.deletedAt == null }
                    .groupBy { it.category }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // ------------------------------------------------------------
    // BOOTSTRAP
    // ------------------------------------------------------------

    fun bootstrap(deepLinkListId: String?, deepLinkInviteId: String?) {

        android.util.Log.d("BOOTSTRAP", "Bootstrap entered")

        viewModelScope.launch {

            try {
                android.util.Log.d("BOOTSTRAP", "Ensuring authentication")
                authProvider.ensureAuthenticated()

                android.util.Log.d("BOOTSTRAP", "Ensuring user document")
                repository.ensureUserDocument()

                // 🔥 DeepLink wird aktuell noch ignoriert (Minimal stabiler Start)
                val listId = repository.createOrGetDefaultList()

                android.util.Log.d("BOOTSTRAP", "ListId = $listId")

                // 🔥 Membership garantieren
                repository.awaitMembership(listId)

                android.util.Log.d("BOOTSTRAP", "Membership OK")

                repository.setActiveList(listId)

                android.util.Log.d("BOOTSTRAP", "Active list set")

                repository.observeItems()
                    .collect { items ->
                        android.util.Log.d("BOOTSTRAP", "Items received: ${items.size}")
                        _uiItems.value = items
                    }

            } catch (e: Exception) {
                android.util.Log.e("BOOT_FATAL", "Bootstrap crashed", e)
                throw e
            }
        }
    }

    // ------------------------------------------------------------
    // EVENTS
    // ------------------------------------------------------------

    fun onEvent(event: ShopEvent) {
        when (event) {

            is ShopEvent.AddItem ->
                addItemInternal(event.name)

            is ShopEvent.ToggleItem ->
                toggleItemInternal(event.item)

            is ShopEvent.DeleteItem ->
                deleteItemInternal(event.item)

            ShopEvent.ClearAll ->
                clearAllInternal()

            is ShopEvent.UpdateItem ->
                updateItemOptimistic(event.item, event.newName)

            is ShopEvent.CreateInvite ->
                createInviteInternal(event.context)

            is ShopEvent.ClearAll ->
                clearAllInternal()

            else -> Unit
        }
    }

    private fun addItemInternal(name: String) {

        if (name.isBlank()) return

        viewModelScope.launch {

            val normalizedName = quantityMapper.normalize(name)
            val mappedCategory = categoryMapper.resolve(normalizedName)

            val item = ShoppingItemEntity(
                id = UUID.randomUUID().toString(),
                name = normalizedName,
                quantity = 1,
                category = mappedCategory,
                isChecked = true,
                version = 0,
                deletedAt = null,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            repository.addItem(item)
        }
    }

    private fun toggleItemInternal(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.updateItem(item.copy(isChecked = !item.isChecked))
        }
    }

    private fun deleteItemInternal(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.softDelete(item.id)
        }
    }

    private fun clearAllInternal() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    private fun updateItemInternal(
        item: ShoppingItemEntity,
        newName: String
    ) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.updateItem(item.copy(name = newName))
        }
    }

    private fun createInviteInternal(context: android.content.Context) {

        val listId = repository.currentListId.value ?: return

        viewModelScope.launch {

            val inviteId = repository.createInvite(listId)

            val inviteLink =
                "https://shopme-app.de/invite?listId=$listId&inviteId=$inviteId"

            val sendIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(
                    android.content.Intent.EXTRA_TEXT,
                    "Ich lade dich zu meiner ShopMe Liste ein:\n$inviteLink"
                )
                type = "text/plain"
            }

            context.startActivity(
                android.content.Intent.createChooser(sendIntent, "Liste teilen")
            )
        }
    }

    private fun updateItemOptimistic(
        item: ShoppingItemEntity,
        newName: String
    ) {
        if (newName.isBlank()) return

        viewModelScope.launch {
            repository.updateItem(
                item.copy(name = newName)
            )
        }
    }
}