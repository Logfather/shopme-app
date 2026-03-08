package de.shopme.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.shopme.data.repository.FirestoreShoppingRepository
import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.auth.AuthProvider
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType
import de.shopme.presentation.shopping.ShoppingUiState
import de.shopme.presentation.event.ShopEvent
import de.shopme.core.mapper.CategoryMapper
import de.shopme.core.network.NetworkMonitor
import de.shopme.core.mapper.QuantityMapper
import de.shopme.domain.usecase.CreateListUseCase
import de.shopme.domain.usecase.DeleteListUseCase
import de.shopme.domain.usecase.SetActiveListUseCase
import de.shopme.presentation.action.ShoppingAction
import de.shopme.presentation.shopping.ShoppingViewState
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import de.shopme.presentation.reducer.reduce
import de.shopme.presentation.effect.UIEffect

class ShoppingViewModel(
    private val createListUseCase: CreateListUseCase,
    private val deleteListUseCase: DeleteListUseCase,
    private val setActiveListUseCase: SetActiveListUseCase,
    private val repository: FirestoreShoppingRepository,
    private val quantityMapper: QuantityMapper,
    private val categoryMapper: CategoryMapper,
    private val networkMonitor: NetworkMonitor,
    private val authProvider: AuthProvider,
) : ViewModel() {

    // ------------------------------------------------------------
    // BASE STATE
    // ------------------------------------------------------------

    private val _userLists =
        MutableStateFlow<List<ShoppingListEntity>>(emptyList())

    val userLists: StateFlow<List<ShoppingListEntity>> =
        _userLists.asStateFlow()

    private val _uiState =
        MutableStateFlow<ShoppingUiState>(ShoppingUiState.Loading)

    val uiState: StateFlow<ShoppingUiState> =
        _uiState.asStateFlow()

    private val _showWelcomeDialog =
        MutableStateFlow(true)

    val showWelcomeDialog: StateFlow<Boolean> =
        _showWelcomeDialog.asStateFlow()

    private val _effects =
        MutableSharedFlow<UIEffect>()

    val effects: SharedFlow<UIEffect> =
        _effects

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
            SharingStarted.WhileSubscribed(5000),
            null
        )

    // ------------------------------------------------------------
    // ITEMS
    // ------------------------------------------------------------

    private val _uiItems =
        MutableStateFlow<List<ShoppingItem>>(emptyList())

    val uiItems: StateFlow<List<ShoppingItem>> =
        _uiItems.asStateFlow()

    val groupedItems: StateFlow<Map<String, List<ShoppingItem>>> =
        uiItems
            .map { items ->
                items
                    .filter { it.deletedAt == null }
                    .groupBy { it.category }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyMap()
            )

    val viewState: StateFlow<ShoppingViewState> =
        combine(
            combine(
                uiState,
                userLists,
                activeList
            ) { state, lists, active ->
                Triple(state, lists, active)
            },
            combine(
                groupedItems,
                showWelcomeDialog
            ) { items, welcome ->
                Pair(items, welcome)
            }
        ) { left, right ->

            val (state, lists, active) = left
            val (items, welcome) = right

            ShoppingViewState(
                uiState = state,
                lists = lists,
                activeList = active,
                groupedItems = items,
                showWelcomeDialog = welcome,
                showStoreSelectionDialog = state is ShoppingUiState.MultiSelect
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ShoppingViewState()
        )

    // ------------------------------------------------------------
    // INIT
    // ------------------------------------------------------------

    init {
        observeInitialState()
        observeActiveListGuard()
    }

    val existingStores: StateFlow<List<StoreType>> =
        userLists
            .map { lists ->
                lists
                    .flatMap { it.storeTypes }
                    .distinct()
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    private fun observeInitialState() {
        viewModelScope.launch {
            combine(
                repository.currentListId,
                _userLists
            ) { currentId, lists ->
                currentId to lists
            }.collect { (currentId, lists) ->

                if (_uiState.value != ShoppingUiState.Loading) return@collect

                when {

                    lists.isEmpty() -> {
                        transitionTo(ShoppingUiState.MultiOverview)
                    }

                    currentId != null &&
                            lists.any { it.id == currentId } -> {
                        transitionTo(ShoppingUiState.Normal)
                    }

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

            ShoppingUiState.MultiOverview -> Unit

            is ShoppingUiState.MultiSelect -> Unit

            ShoppingUiState.Loading -> Unit
        }

        _uiState.value = newState
    }

    // ------------------------------------------------------------
    // BOOTSTRAP
    // ------------------------------------------------------------

    fun bootstrap(
        deepLinkListId: String?,
        deepLinkInviteId: String?
    ) {

        viewModelScope.launch {

            authProvider.ensureAuthenticated()

            repository.ensureUserDocument()

            launch {

                repository.observeListsForUser()
                    .collect { lists ->

                        _userLists.value = lists

                        Log.d(
                            "FLOW_DEBUG",
                            "observeListsForUser emitted: ${lists.size}"
                        )

                        if (
                            repository.currentListId.value == null &&
                            lists.isNotEmpty()
                        ) {
                            setActiveListUseCase(lists.first().id)
                        }
                    }
            }

            launch {

                uiState
                    .flatMapLatest { state ->

                        if (state is ShoppingUiState.Normal) {

                            repository.observeItems()

                        } else {

                            flowOf(emptyList())

                        }
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

        transitionTo(
            ShoppingUiState.MultiSelect(existingStores.value.toList())
        )
    }

    fun toggleStore(store: StoreType) {

        val state = _uiState.value

        if (state is ShoppingUiState.MultiSelect) {

            val updated =
                state.selectedStores.toMutableList().apply {
                    if (contains(store)) remove(store)
                    else add(store)
                }

            _uiState.value =
                ShoppingUiState.MultiSelect(updated)
        }
    }

    fun confirmStoreSelection(customLists: List<String>) {

        val state = _uiState.value

        if (state is ShoppingUiState.MultiSelect) {

            if (state.selectedStores.isNotEmpty()) {
                createAllLists(state.selectedStores)
            }

            viewModelScope.launch {

                customLists.forEach { name ->

                    createListUseCase(
                        name = name,
                        storeTypes = emptyList(),
                        isCustom = true
                    )
                }
            }
        }
    }

    fun createListForStore(store: StoreType) {

        viewModelScope.launch {

            val listName = "${store.displayName} Einkauf"

            createListUseCase(
                name = listName,
                storeTypes = listOf(store),
                isCustom = false
            )

            val state = _uiState.value

            if (state is ShoppingUiState.MultiSelect) {

                val remaining =
                    state.selectedStores.toMutableList().apply {
                        remove(store)
                    }

                if (remaining.isEmpty()) {

                    transitionTo(ShoppingUiState.MultiOverview)

                } else {

                    _uiState.value =
                        ShoppingUiState.MultiSelect(remaining)
                }
            }
        }
    }

    fun createAllLists(stores: List<StoreType>) {

        if (stores.isEmpty()) return

        Log.d("CREATE_FLOW", "createAllLists() called")

        viewModelScope.launch {

            val storesToCreate =
                stores.filter { it !in existingStores.value }

            var lastCreatedListId: String? = null

            for (store in storesToCreate) {

                val listName = "${store.displayName} Einkauf"

                val newListId =
                    createListUseCase(
                        name = listName,
                        storeTypes = listOf(store),
                        isCustom = false
                    )

                lastCreatedListId = newListId
            }

            lastCreatedListId?.let {
                setActiveListUseCase(it)
            }

            _effects.emit(
                UIEffect.ShowSnackbar("Listen wurden erstellt")
            )

            transitionTo(ShoppingUiState.MultiOverview)
        }
    }

    fun addMoreStores() {
        dispatch(ShoppingAction.StartMultiStoreCreation)
    }

    fun cancelMultiCreation() {
        dispatch(ShoppingAction.CancelMultiCreation)
    }

    fun editList(list: ShoppingListEntity) {
        setActiveList(list)
        transitionTo(ShoppingUiState.Normal)
    }

    fun deleteList(list: ShoppingListEntity) {
        viewModelScope.launch {
            deleteListUseCase(list.id)
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

            val normalizedName =
                quantityMapper.normalize(name)

            val mappedCategory =
                categoryMapper.resolve(normalizedName)

            val item =
                ShoppingItemEntity(
                    id = UUID.randomUUID().toString(),
                    name = normalizedName,
                    quantity = 1,
                    category = mappedCategory,
                    isChecked = true,
                    version = 0,
                    deletedAt = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

            repository.addItem(item)
        }
    }

    private fun toggleItemInternal(item: ShoppingItem) {

        viewModelScope.launch {

            repository.updateItem(
                item.toEntity().copy(isChecked = !item.isChecked)
            )
        }
    }

    private fun deleteItemInternal(item: ShoppingItem) {

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
        item: ShoppingItem,
        newName: String
    ) {
        if (newName.isBlank()) return

        viewModelScope.launch {

            repository.updateItem(
                item.toEntity().copy(name = newName)
            )

        }
    }

    private fun handleSideEffects(action: ShoppingAction) {

        when (action) {

            is ShoppingAction.ConfirmStores -> {

                viewModelScope.launch {
                    confirmStoreSelection(action.customLists)
                }
            }

            is ShoppingAction.DeleteList -> {

                viewModelScope.launch {
                    deleteList(action.list)
                }
            }

            else -> Unit
        }
    }

    fun dismissWelcomeDialog() {
        _showWelcomeDialog.value = false
    }

    fun setActiveList(list: ShoppingListEntity) {

        viewModelScope.launch {

            setActiveListUseCase(list.id)
        }
    }

    fun dispatch(action: ShoppingAction) {

        val currentState = _uiState.value

        val newState =
            reduce(currentState, action)

        _uiState.value = newState

        handleSideEffects(action)
    }

    private fun ShoppingItem.toEntity(): ShoppingItemEntity =
        ShoppingItemEntity(
            id = id,
            name = name,
            quantity = quantity,
            category = category,
            isChecked = isChecked,
            version = version,
            deletedAt = deletedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
}