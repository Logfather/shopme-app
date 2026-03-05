package de.shopme.presentation.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import de.shopme.data.FirestoreShoppingRepository
import de.shopme.data.ShoppingItemEntity
import de.shopme.domain.auth.AuthProvider
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType
import de.shopme.presentation.shopping.ShoppingUiState
import de.shopme.ui.ShopEvent
import de.shopme.util.CategoryMapper
import de.shopme.util.NetworkMonitor
import de.shopme.util.QuantityMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
class ShoppingViewModel(
    private val repository: FirestoreShoppingRepository,
    private val quantityMapper: QuantityMapper,
    private val categoryMapper: CategoryMapper,
    private val networkMonitor: NetworkMonitor,
    private val authProvider: AuthProvider,
) : ViewModel() {
    // ------------------------------------------------------------
    // BASE STATE (MUSS VOR INIT STEHEN)
    // ------------------------------------------------------------

    private val _userLists =
        MutableStateFlow<List<ShoppingListEntity>>(emptyList())

    val userLists: StateFlow<List<ShoppingListEntity>> =
        _userLists.asStateFlow()

    private val _uiState =
        MutableStateFlow<ShoppingUiState>(ShoppingUiState.Loading)

    val uiState: StateFlow<ShoppingUiState> =
        _uiState.asStateFlow()

    private val _selectedStores =
        MutableStateFlow<List<StoreType>>(emptyList())

    val selectedStores: StateFlow<List<StoreType>> =
        _selectedStores.asStateFlow()

    private val _snackbarMessage =
        MutableStateFlow<String?>(null)

    val snackbarMessage: StateFlow<String?> =
        _snackbarMessage.asStateFlow()

    // ------------------------------------------------------------
    // DERIVED STATE
    // ------------------------------------------------------------
    val activeList: StateFlow<ShoppingListEntity?> =
        combine(
            repository.currentListId,
            userLists
        ) { currentId, lists ->
            lists.firstOrNull { it.id == currentId }
        }.stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            null
        )
    // ------------------------------------------------------------
    // ITEMS
    // ------------------------------------------------------------
    private val _uiItems =
        MutableStateFlow<List<ShoppingItemEntity>>(emptyList())
    val uiItems: StateFlow<List<ShoppingItemEntity>> =
        _uiItems.asStateFlow()
    val groupedItems: StateFlow<Map<String, List<ShoppingItemEntity>>> =
        uiItems
            .map { items ->
                items
                    .filter { it.deletedAt == null }
                    .groupBy { it.category }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.Companion.WhileSubscribed(5000),
                emptyMap()
            )
    // ------------------------------------------------------------
    // INIT
    // ------------------------------------------------------------
    init {
        observeInitialState()
        observeActiveListGuard()
    }
    private fun observeInitialState() {
        viewModelScope.launch {
            combine(
                repository.currentListId,
                _userLists
            ) { currentId, lists ->
                currentId to lists
            }.collect { (currentId, lists) ->

                // Nur während Loading reagieren
                if (_uiState.value != ShoppingUiState.Loading) return@collect

                when {

                    // -------------------------------------------------
                    // FIRST START → keine Listen vorhanden
                    // -------------------------------------------------
                    lists.isEmpty() -> {

                        _snackbarMessage.value =
                            "Willkommen bei ShopMe"

                        transitionTo(ShoppingUiState.MultiOverview)
                    }

                    // -------------------------------------------------
                    // aktive Liste vorhanden
                    // -------------------------------------------------
                    currentId != null &&
                            lists.any { it.id == currentId } -> {

                        transitionTo(ShoppingUiState.Normal)
                    }

                    // -------------------------------------------------
                    // Listen vorhanden aber keine aktiv
                    // -------------------------------------------------
                    else -> {

                        transitionTo(ShoppingUiState.MultiOverview)
                    }
                }
            }
        }
    }
    private fun observeActiveListGuard() {
        viewModelScope.launch {
            activeList.collect { list ->
                if (_uiState.value == ShoppingUiState.Normal && list == null) {
                    transitionTo(ShoppingUiState.MultiOverview)
                }
            }
        }
    }
    private fun transitionTo(newState: ShoppingUiState) {
        when (newState) {
            ShoppingUiState.Normal ->
                check(activeList.value != null)
            ShoppingUiState.MultiOverview -> {
                // MultiOverview darf auch leer sein (Empty-State Screen)
            }
            ShoppingUiState.MultiSelect,
            ShoppingUiState.Loading -> Unit
        }
        _uiState.value = newState
    }
    // ------------------------------------------------------------
    // BOOTSTRAP
    // ------------------------------------------------------------
    fun bootstrap(deepLinkListId: String?, deepLinkInviteId: String?) {
        viewModelScope.launch {
            authProvider.ensureAuthenticated()
            repository.ensureUserDocument()
            launch {
                repository.observeListsForUser()
                    .collect { lists ->
                        _userLists.value = lists
                        Log.d("FLOW_DEBUG", "observeListsForUser emitted: ${lists.size}")
                        if (repository.currentListId.value == null && lists.isNotEmpty()) {
                            repository.setActiveList(lists.first().id)
                        }
                    }
            }
            launch {
                uiState
                    .flatMapLatest { state: ShoppingUiState ->
                        if (state == ShoppingUiState.Normal)
                            repository.observeItems()
                        else
                            flowOf(emptyList())
                    }
                    .collect { items ->
                        _uiItems.value = items
                    }
            }
        }
    }
    // ------------------------------------------------------------
    // MULTISTORE FLOW
    // ------------------------------------------------------------
    fun startMultiStoreCreation() {

        val existingStores =
            _userLists.value
                .flatMap { it.storeTypes }
                .distinct()

        _selectedStores.value = existingStores

        transitionTo(ShoppingUiState.MultiSelect)
    }
    fun toggleStore(store: StoreType) {
        _selectedStores.value =
            _selectedStores.value.toMutableList().apply {
                if (contains(store)) remove(store) else add(store)
            }
    }
    fun confirmStoreSelection(customLists: List<String>) {

        if (_selectedStores.value.isNotEmpty()) {
            createAllLists()
        }

        viewModelScope.launch {

            customLists.forEach { name ->

                repository.createList(
                    name = name,
                    storeTypes = emptyList(),
                    isCustom = true
                )
            }
        }
    }
    fun createListForStore(store: StoreType) {
        viewModelScope.launch {
            val newListId = repository.createList(
                name = "${store.displayName} Einkauf",
                storeTypes = listOf(store)
            )
            repository.setActiveList(newListId)
            val remaining = _selectedStores.value.toMutableList()
            remaining.remove(store)
            _selectedStores.value = remaining
            if (remaining.isEmpty()) {
                transitionTo(ShoppingUiState.MultiOverview)
            }
        }
    }
    fun createAllLists() {

        if (_selectedStores.value.isEmpty()) return

        Log.d("CREATE_FLOW", "createAllLists() called")

        viewModelScope.launch {

            val existingStores =
                _userLists.value.flatMap { it.storeTypes }

            val storesToCreate =
                _selectedStores.value.filter {
                    it !in existingStores
                }

            var lastCreatedListId: String? = null

            for (store in storesToCreate) {

                val listName = "${store.displayName} Einkauf"

                val newListId = repository.createList(
                    name = listName,
                    storeTypes = listOf(store)
                )

                lastCreatedListId = newListId
            }

            lastCreatedListId?.let {
                repository.setActiveList(it)
            }

            _selectedStores.value = emptyList()
            _snackbarMessage.value = "Listen wurden erstellt"

            transitionTo(ShoppingUiState.MultiOverview)
        }
    }
    fun snackbarShown() {
        _snackbarMessage.value = null
    }
    fun addMoreStores() {
        transitionTo(ShoppingUiState.MultiSelect)
    }
    fun cancelMultiCreation() {
        _selectedStores.value = emptyList()
        transitionTo(ShoppingUiState.MultiOverview)
    }
    fun editList(list: ShoppingListEntity) {
        viewModelScope.launch {
            repository.setActiveList(list.id)
            transitionTo(ShoppingUiState.Normal)
        }
    }
    fun deleteList(list: ShoppingListEntity) {
        viewModelScope.launch {
            repository.deleteList(list.id)
        }
    }


    // ------------------------------------------------------------
    // EVENTS
    // ------------------------------------------------------------
    fun onEvent(event: ShopEvent) {
        when (event) {
            is ShopEvent.AddItem -> addItemInternal(event.name)
            is ShopEvent.ToggleItem -> toggleItemInternal(event.item)
            is ShopEvent.DeleteItem -> deleteItemInternal(event.item)
            ShopEvent.ClearAll -> clearAllInternal()
            is ShopEvent.UpdateItem -> updateItemOptimistic(event.item, event.newName)
            else -> Unit
        }
    }
    // ------------------------------------------------------------
    // ITEM LOGIC
    // ------------------------------------------------------------
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
                createdAt = Timestamp.Companion.now(),
                updatedAt = Timestamp.Companion.now()
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
    private fun updateItemOptimistic(
        item: ShoppingItemEntity,
        newName: String
    ) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.updateItem(item.copy(name = newName))
        }
    }
}
