package de.shopme.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.shopme.core.network.NetworkMonitor
import de.shopme.data.mapper.EntityMapper.toDomain
import de.shopme.data.mapper.EntityMapper.toEntity
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.domain.auth.AuthProvider
import de.shopme.domain.model.*
import de.shopme.domain.service.CategoryMapper
import de.shopme.domain.service.QuantityMapper
import de.shopme.domain.service.SpeechItemParser
import de.shopme.domain.usecase.CreateListUseCase
import de.shopme.domain.usecase.DeleteListUseCase
import de.shopme.presentation.action.ShoppingAction
import de.shopme.presentation.effect.UIEffect
import de.shopme.presentation.event.ShopEvent
import de.shopme.presentation.state.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ShoppingViewModel(
    private val createListUseCase: CreateListUseCase,
    private val deleteListUseCase: DeleteListUseCase,
    private val roomRepository: RoomShoppingRepository,
    private val quantityMapper: QuantityMapper,
    private val categoryMapper: CategoryMapper,
    private val networkMonitor: NetworkMonitor,
    private val authProvider: AuthProvider,
    private val speechItemParser: SpeechItemParser
) : ViewModel() {

    // ------------------------------------------------------------
    // STATE
    // ------------------------------------------------------------

    private val _state = MutableStateFlow(ShoppingState())
    val state: StateFlow<ShoppingState> = _state.asStateFlow()

    private val _showWelcomeDialog = MutableStateFlow(true)
    val showWelcomeDialog: StateFlow<Boolean> = _showWelcomeDialog.asStateFlow()

    private val _effects = MutableSharedFlow<UIEffect>()
    val effects: SharedFlow<UIEffect> = _effects

    private val _currentListId = MutableStateFlow<String?>(null)
    val currentListId: StateFlow<String?> = _currentListId



    fun setCurrentList(listId: String) {

        Log.d("LIST_DEBUG", "SET currentListId=$listId")

        _currentListId.value = listId

        _state.update {
            it.copy(activeListId = listId)
        }
    }

    // ------------------------------------------------------------
    // VIEW STATE
    // ------------------------------------------------------------

    val viewState: StateFlow<ShoppingViewState> =
        combine(state, showWelcomeDialog) { s, welcome ->

            val active =
                s.lists.firstOrNull { it.id == s.activeListId }

            val grouped =
                s.items
                    .filter { it.deletedAt == null }
                    .groupBy { it.category }

            ShoppingViewState(
                uiState = s.screenMode,
                lists = s.lists,
                activeList = active,
                groupedItems = grouped,
                showWelcomeDialog = welcome,
                showStoreSelectionDialog =
                    s.screenMode is ShoppingScreenMode.MultiSelect
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ShoppingViewState()
        )

    // ------------------------------------------------------------
    // BOOTSTRAP
    // ------------------------------------------------------------

    fun bootstrap(
        deepLinkListId: String?,
        deepLinkInviteId: String?
    ) {
        viewModelScope.launch {

            authProvider.ensureAuthenticated()

            val uid = authProvider.currentUserId()
                ?: return@launch

            launch { observeLists() }
        }
    }

    private suspend fun observeLists() {

        roomRepository.observeLists()
            .collect { lists ->

                val domainLists =
                    lists.map { it.toDomain() }

                _state.update { current ->
                    current.copy(
                        lists = domainLists,
                        activeListId =
                            if (domainLists.any { it.id == current.activeListId })
                                current.activeListId
                            else
                                null
                    )
                }

                if (domainLists.isEmpty()) {

                    _showWelcomeDialog.value = true

                    _state.update {
                        it.copy(
                            screenMode =
                                ShoppingScreenMode.MultiOverview
                        )
                    }

                } else {

                    _showWelcomeDialog.value = false
                }
            }
    }

    init {
        observeItems()
    }

    private fun observeItems() {

        viewModelScope.launch {

            currentListId
                .onEach { Log.d("EVENT_DEBUG", "Flow emits listId=$it") }
                .filterNotNull()
                .collectLatest { listId ->

                    Log.d("EVENT_DEBUG", "OBSERVE → listId=$listId")

                    roomRepository
                        .observeItemsWithSyncStatus(listId)
                        .collect { itemsWithStatus ->

                            Log.d("EVENT_DEBUG", "Items size=${itemsWithStatus.size}") // ✅ HIER

                            val domainItems = itemsWithStatus.map { (entity, status) ->
                                entity.toDomain().copy(syncStatus = status)
                            }

                            _state.update {
                                it.copy(items = domainItems)
                            }
                        }
                }
        }
    }

    // ------------------------------------------------------------
    // REDUCER
    // ------------------------------------------------------------

    fun dispatch(action: ShoppingAction) {

        val result =
            de.shopme.presentation.reducer.reduce(
                state = _state.value,
                screenMode = _state.value.screenMode,
                action = action
            )

        _state.value = result.state

        result.effects.forEach { effect ->
            handleEffect(effect)
        }
    }

    private fun handleEffect(effect: UIEffect) {

        when (effect) {

            is UIEffect.AddItem -> {
                Log.d("EVENT_DEBUG", "Effect → AddItem: ${effect.name}")
                addItem(effect.name)
            }

            is UIEffect.ToggleItem -> {
                toggleItem(effect.item)
            }

            is UIEffect.DeleteItem -> {
                deleteItem(effect.item)
            }

            is UIEffect.CreateLists -> {
                confirmStoreSelection(
                    stores = effect.stores,
                    customLists = effect.customLists
                )
            }

            is UIEffect.DeleteList -> {
                deleteList(effect.list.toDomain())
            }

            is UIEffect.ShowSnackbar -> {
                viewModelScope.launch {
                    _effects.emit(effect)
                }
            }

            UIEffect.ShowWelcomeDialog -> {
                _showWelcomeDialog.value = true
            }

            UIEffect.HideWelcomeDialog -> {
                _showWelcomeDialog.value = false
            }
        }
    }

    // ------------------------------------------------------------
    // LIST MANAGEMENT
    // ------------------------------------------------------------

    private fun setActiveList(listId: String) {
        setCurrentList(listId)
    }

    fun editList(list: ShoppingList) {

        setActiveList(list.id)

        _state.update {
            it.copy(screenMode = ShoppingScreenMode.Normal)
        }
    }

    fun deleteList(list: ShoppingList) {

        viewModelScope.launch {
            deleteListUseCase(list.id)
        }
    }

    private fun confirmStoreSelection(
        stores: List<StoreType>,
        customLists: List<String>

    ) {
        viewModelScope.launch {

            var lastCreatedId: String? = null

            stores.forEach { store ->

                lastCreatedId =
                    createListUseCase(
                        name = store.displayName,
                        storeTypes = listOf(store),
                        isCustom = false
                    )
            }

            customLists.forEach { name ->

                lastCreatedId =
                    createListUseCase(
                        name = name,
                        storeTypes = emptyList(),
                        isCustom = true
                    )
            }

            lastCreatedId?.let { id ->

                setCurrentList(id)   // 🔥 DAS IST DER FIX

                _state.update {
                    it.copy(
                        screenMode = ShoppingScreenMode.MultiOverview
                    )
                }
            }
            Log.d("EVENT_DEBUG", "Created listId=$lastCreatedId")
        }
    }

    // ------------------------------------------------------------
    // ITEM LOGIC
    // ------------------------------------------------------------

    fun onEvent(event: ShopEvent) {

        Log.d("EVENT_DEBUG", "Event received: $event")

        val result =
            de.shopme.presentation.reducer.reduce(
                state = _state.value,
                screenMode = _state.value.screenMode,
                event = event
            )

        _state.value = result.state

        // 🔥 DAS ist der richtige Weg
        result.effects.forEach { effect ->
            handleEffect(effect)
        }
    }

    private fun addItem(name: String) {

        if (name.isBlank()) return

        viewModelScope.launch {

            val normalized =
                quantityMapper.normalize(name)

            val category =
                categoryMapper.resolve(normalized)

            val listId =
                currentListId.value ?: return@launch

            val item = ShoppingItemEntity(
                id = UUID.randomUUID().toString(),
                listId = listId,
                name = normalized,
                quantity = 1,
                category = category,
                isChecked = true,
                version = 0,
                deletedAt = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            Log.d("EVENT_DEBUG", "addItem: Insert item into listId=$listId")

            Log.d("EVENT_DEBUG", "ADD → currentListId=$listId")
            Log.d("EVENT_DEBUG", "ADD → activeListId=${state.value.activeListId}")

            roomRepository.addItem(item)
        }
    }

    private fun toggleItem(item: ShoppingItem) {

        viewModelScope.launch {

            roomRepository.updateItem(
                item.toEntity().copy(
                    isChecked = !item.isChecked
                )
            )
        }
    }

    private fun deleteItem(item: ShoppingItem) {

        viewModelScope.launch {

            roomRepository.deleteItem(item.toEntity())
        }
    }

    // ------------------------------------------------------------
    // SPEECH
    // ------------------------------------------------------------

    fun addItemsFromSpeech(text: String) {

        speechItemParser.parseSpeech(text)
            .forEach { parsed ->

                repeat(parsed.quantity) {

                    onEvent(
                        ShopEvent.Item.Add(parsed.name)
                    )
                }
            }
    }

    // ------------------------------------------------------------
    // MULTI STORE FLOW
    // ------------------------------------------------------------

    fun toggleStore(store: StoreType) {

        val mode = state.value.screenMode

        if (mode !is ShoppingScreenMode.MultiSelect) return

        val updated =
            if (store in mode.selectedStores)
                mode.selectedStores - store
            else
                mode.selectedStores + store

        _state.update {
            it.copy(
                screenMode =
                    ShoppingScreenMode.MultiSelect(updated)
            )
        }
    }

    fun dismissWelcomeDialog() {
        _showWelcomeDialog.value = false
    }

    fun startMultiStoreCreation() {

        val existingStores =
            state.value.lists
                .flatMap { it.storeTypes }
                .distinct()

        _state.update {
            it.copy(
                screenMode =
                    ShoppingScreenMode.MultiSelect(existingStores)
            )
        }
    }
}