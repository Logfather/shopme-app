package de.shopme.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.shopme.core.network.NetworkMonitor
import de.shopme.data.datasource.firestore.FirestoreDataSource
import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import de.shopme.data.mapper.EntityMapper.toDomain
import de.shopme.data.mapper.EntityMapper.toEntity
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.FirestoreListener
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
import de.shopme.presentation.undo.UndoAction
import kotlinx.coroutines.delay
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
    private val speechItemParser: SpeechItemParser,
    private val firestoreDataSource: FirestoreDataSource,
    private val itemDao: ItemDao,
    private val listDao: ListDao,
    private val firestoreListener: FirestoreListener
) : ViewModel() {

    // ------------------------------------------------------------
    // STATE
    // ------------------------------------------------------------

    private val _state = MutableStateFlow(ShoppingState())
    val state: StateFlow<ShoppingState> = _state.asStateFlow()

    private val _showWelcomeDialog = MutableStateFlow(true)
    val showWelcomeDialog: StateFlow<Boolean> = _showWelcomeDialog.asStateFlow()

    val showAccountAction = MutableStateFlow(false)
    val isAnonymous = MutableStateFlow(true)

    private val _effects = MutableSharedFlow<UIEffect>()
    val effects: SharedFlow<UIEffect> = _effects

    private val _currentListId = MutableStateFlow<String?>(null)
    val currentListId: StateFlow<String?> = _currentListId

    private var lastUndoAction: UndoAction? = null
    private var pendingInviteListId: String? = null
    private var _accountHintShown = false

    val shouldShowAccountHint = MutableStateFlow(false)

    init {
        observeItems()
    }

    fun itemsForList(listId: String) =
        roomRepository.observeItems(listId)

    fun consumePendingInvite(context: Context) {

        val prefs = context.getSharedPreferences("shopme", Context.MODE_PRIVATE)
        val listId = prefs.getString("pending_invite_list_id", null)

        if (listId != null) {

            val exists = state.value.lists.any { it.id == listId }

            prefs.edit().remove("pending_invite_list_id").apply()

            pendingInviteListId = listId

            if (exists) {
                setCurrentList(listId)
            } else {
                joinList(listId, null)
            }
        }
    }

    fun setCurrentList(listId: String) {
        _currentListId.value = listId

        _state.update {
            it.copy(activeListId = listId)
        }
    }

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
                    s.screenMode is ShoppingScreenMode.MultiSelect,
                snackbarMessage = null
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ShoppingViewState()
        )

    fun editList(list: ShoppingList) {
        setCurrentList(list.id)

        _state.update {
            it.copy(screenMode = ShoppingScreenMode.Normal)
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
        result.effects.forEach { effect -> handleEffect(effect) }
    }

    fun deleteList(list: ShoppingList) {
        viewModelScope.launch {

            val snapshot = deleteListUseCase(list.id)

            val action = UndoAction.DeleteList(snapshot)

            lastUndoAction = action

            handleEffect(
                UIEffect.ShowUndo(
                    action = action,
                    message = "Liste gelöscht"
                )
            )
        }
    }

    fun bootstrap(
        deepLinkListId: String? = null,
        deepLinkInviteId: String? = null
    ) {

        viewModelScope.launch {

            val uid = authProvider.getCurrentUserId() ?: return@launch

            isAnonymous.value = authProvider.isAnonymous()

            firestoreListener.startListSync(uid)
            observeLists()
        }
    }

    private fun observeItems() {

        viewModelScope.launch {

            currentListId
                .filterNotNull()
                .collectLatest { listId ->

                    firestoreListener.startItemSync(listId)

                    roomRepository
                        .observeItemsWithSyncStatus(listId)
                        .collect { itemsWithStatus ->

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

    private fun observeLists() {

        viewModelScope.launch {

            roomRepository.observeLists()
                .collect { lists ->

                    val domainLists =
                        lists
                            .map { it.toDomain() }
                            .filter { it.name.isNotBlank() }

                    if (domainLists.size == 1 && !_accountHintShown) {
                        shouldShowAccountHint.value = true
                        _accountHintShown = true
                    }

                    _state.update { current ->

                        val validActiveId =
                            current.activeListId
                                ?.takeIf { id -> domainLists.any { it.id == id } }

                        val newActiveId =
                            validActiveId ?: domainLists.firstOrNull()?.id

                        current.copy(
                            lists = domainLists,
                            screenMode = ShoppingScreenMode.MultiOverview,
                            activeListId = newActiveId
                        )
                    }

                    _showWelcomeDialog.value = domainLists.isEmpty()
                }
        }
    }

    private fun handleEffect(effect: UIEffect) {

        when (effect) {

            is UIEffect.AddItem -> addItem(effect.name)

            is UIEffect.ToggleItem -> toggleItem(effect.item)

            is UIEffect.DeleteItem -> deleteItem(effect.item)

            is UIEffect.CreateLists -> {
                confirmStoreSelection(
                    stores = effect.stores,
                    customLists = effect.customLists
                )
            }

            is UIEffect.RetrySync -> {
                viewModelScope.launch {
                    roomRepository.retryChangeByItemId(effect.itemId)
                }
            }

            is UIEffect.ShowUndo -> {

                lastUndoAction = effect.action

                viewModelScope.launch {

                    _effects.emit(effect)

                    val currentAction = effect.action

                    delay(3000)

                    if (lastUndoAction === currentAction) {
                        lastUndoAction = null
                    }
                }
            }

            is UIEffect.ShowSnackbar -> {
                // no-op
            }

            is UIEffect.UpdateItem -> {
                updateItem(effect.item, effect.newName)
            }

            UIEffect.ShowWelcomeDialog -> {
                _showWelcomeDialog.value = true
            }

            UIEffect.HideWelcomeDialog -> {
                _showWelcomeDialog.value = false
            }

            is UIEffect.DeleteAllLists -> {
                viewModelScope.launch {
                    roomRepository.deleteAllLists()
                }
            }

            is UIEffect.RequestDeleteList -> {

                val list = _state.value.lists.firstOrNull { it.id == effect.listId }

                if (list == null) {
                    Log.w("DELETE", "List not found in state: ${effect.listId}")
                    return
                }

                deleteList(list)
            }
        }
    }

    private fun updateItem(item: ShoppingItem, newName: String) {

        viewModelScope.launch {

            val updated = item.copy(
                name = newName,
                isChecked = true,
                updatedAt = System.currentTimeMillis()
            )

            roomRepository.updateItem(updated.toEntity())
        }
    }

    private fun confirmStoreSelection(
        stores: List<StoreType>,
        customLists: List<String>
    ) {
        viewModelScope.launch {

            var lastCreatedId: String? = null

            if (stores.isEmpty() && customLists.isEmpty()) {
                return@launch
            }

            stores.forEach { store ->

                val id =
                    createListUseCase(
                        name = store.displayName,
                        storeTypes = listOf(store)
                    )

                lastCreatedId = id

                val list = listDao.getListOnce(id) ?: return@forEach
                val uid = authProvider.currentUserId() ?: return@forEach

                firestoreDataSource.createList(
                    list.copy(ownerId = uid)
                )
            }

            customLists.forEach { name ->

                val id =
                    createListUseCase(
                        name = name,
                        storeTypes = emptyList()
                    )

                lastCreatedId = id

                val list = listDao.getListOnce(id) ?: return@forEach
                val uid = authProvider.currentUserId() ?: return@forEach

                firestoreDataSource.createList(
                    list.copy(ownerId = uid)
                )
            }

            lastCreatedId?.let { id ->

                setCurrentList(id)

                _showWelcomeDialog.value = false

                _state.update {
                    it.copy(
                        screenMode = ShoppingScreenMode.MultiOverview,
                        activeListId = id
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------
    // EVENTS
    // ------------------------------------------------------------

    fun onEvent(event: ShopEvent) {

        if (event is ShopEvent.List.UndoLastAction) {
            handleUndo()
            return
        }

        val result =
            de.shopme.presentation.reducer.reduce(
                state = _state.value,
                screenMode = _state.value.screenMode,
                event = event
            )

        _state.value = result.state
        result.effects.forEach { effect -> handleEffect(effect) }
    }

    private fun handleUndo() {

        val action = lastUndoAction ?: return

        viewModelScope.launch {

            when (action) {

                is UndoAction.DeleteItem -> {
                    val restored = action.item.copy(
                        deletedAt = null,
                        updatedAt = System.currentTimeMillis()
                    )
                    roomRepository.updateItem(restored.toEntity())
                }

                is UndoAction.ToggleItem -> {
                    roomRepository.updateItem(action.item.toEntity())
                }

                is UndoAction.UpdateItem -> {
                    val restored = action.oldItem.copy(
                        isChecked = true,
                        updatedAt = System.currentTimeMillis()
                    )
                    roomRepository.updateItem(restored.toEntity())
                }

                is UndoAction.DeleteList -> {

                    val listId = action.snapshot.list.id

                    roomRepository.restoreList(action.snapshot)

                    setCurrentList(listId)

                    _state.update {
                        it.copy(
                            screenMode = ShoppingScreenMode.Normal,
                            activeListId = listId
                        )
                    }
                }
            }

            lastUndoAction = null
        }
    }

    // ------------------------------------------------------------
    // ITEM LOGIC
    // ------------------------------------------------------------

    private fun addItem(name: String) {

        if (name.isBlank()) return

        viewModelScope.launch {

            val normalized = quantityMapper.normalize(name)
            val category = categoryMapper.resolve(normalized)
            val listId = currentListId.value ?: return@launch

            val item = ShoppingItemEntity(
                id = UUID.randomUUID().toString(),
                listId = listId,
                name = normalized,
                quantity = 1,
                category = category,
                isChecked = true,
                deletedAt = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            roomRepository.addItem(item)
        }
    }

    private fun toggleItem(item: ShoppingItem) {
        viewModelScope.launch {
            roomRepository.updateItem(
                item.toEntity().copy(
                    isChecked = !item.isChecked,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            roomRepository.updateItem(
                item.toEntity().copy(
                    deletedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // ------------------------------------------------------------
    // SPEECH
    // ------------------------------------------------------------

    fun addItemsFromSpeech(text: String) {

        speechItemParser.parseSpeech(text)
            .forEach { parsed ->

                repeat(parsed.quantity) {
                    onEvent(ShopEvent.Item.Add(parsed.name))
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

    fun onRetrySync(itemId: String) {
        viewModelScope.launch {
            roomRepository.retrySyncForItem(itemId)
        }
    }

    fun shareList(listId: String): String {
        val inviteId = UUID.randomUUID().toString()
        return "https://shopme-app.de/invite?listId=$listId&inviteId=$inviteId"
    }

    fun handleInviteLink(uri: Uri) {

        val listId =
            uri.getQueryParameter("listId")
                ?: uri.lastPathSegment?.substringAfter("listId=")?.substringBefore("&")

        val inviteId =
            uri.getQueryParameter("inviteId")
                ?: uri.toString().substringAfter("inviteId=", "").substringBefore("&")

        if (listId == null) return

        joinList(listId, inviteId)
    }

    fun joinList(listId: String, inviteId: String?) {

        viewModelScope.launch {

            try {

                val remoteList = firestoreDataSource.getListOnce(listId)

                remoteList?.let { list ->

                    val uid = authProvider.currentUserId() ?: return@let

                    firestoreDataSource.addUserToList(
                        listId = list.id,
                        userId = uid
                    )

                    delay(300)

                    listDao.upsert(list)

                    firestoreListener.startItemSync(list.id)

                    setCurrentList(list.id)
                }

            } catch (_: Exception) {
            }
        }
    }

    // ------------------------------------------------------------
    // INVITES
    // ------------------------------------------------------------

    fun createInviteAndShare() {

        viewModelScope.launch {

            val inviteId = firestoreDataSource.createInvite()
            val link = "https://shopme.app/invite?inviteId=$inviteId"
        }
    }

    fun joinViaInvite(inviteId: String) {

        viewModelScope.launch {

            try {

                val ownerId = firestoreDataSource.getInvite(inviteId) ?: return@launch
                val uid = authProvider.currentUserId() ?: return@launch

                val lists = firestoreDataSource.getListsForUser(ownerId)

                lists.forEach { list ->
                    firestoreDataSource.addUserToList(
                        listId = list.id,
                        userId = uid
                    )
                }

            } catch (_: Exception) {
            }
        }
    }

    suspend fun linkWithGoogle(idToken: String): Result<Unit> {

        return try {

            val result = authProvider.linkWithGoogle(idToken)

            if (result.isSuccess) {
                isAnonymous.value = false
            }

            result

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}