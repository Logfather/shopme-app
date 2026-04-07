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
import de.shopme.data.sync.ChangeQueue
import de.shopme.data.sync.ChangeQueueDao
import de.shopme.data.sync.ChangeQueueEntity
import de.shopme.data.sync.FirestoreListener
import de.shopme.data.sync.SyncCoordinator
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
    private val firestoreListener: FirestoreListener,
    private val changeQueue: ChangeQueue,
    private val syncCoordinator: SyncCoordinator,
    private val changeQueueDao: ChangeQueueDao
) : ViewModel() {

    private var pendingAuthAction: (() -> Unit)? = null

    private var isSharingInProgress: Boolean = false

    private var pendingShareListIds: List<String>? = null

    private val _state = MutableStateFlow(ShoppingState())
    val state: StateFlow<ShoppingState> = _state.asStateFlow()

    private val _showWelcomeDialog = MutableStateFlow(true)
    val showWelcomeDialog: StateFlow<Boolean> = _showWelcomeDialog.asStateFlow()

    val showAccountAction = MutableStateFlow(false)
    val isAnonymous = MutableStateFlow(true)

    private val _effects = MutableSharedFlow<UIEffect>()
    val effects: SharedFlow<UIEffect> = _effects

    private val _shareEvent = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val shareEvent = _shareEvent.asSharedFlow()

    private val _currentListId = MutableStateFlow<String?>(null)
    val currentListId: StateFlow<String?> = _currentListId

    private var lastUndoAction: UndoAction? = null
    private var pendingInviteListId: String? = null
    private var _accountHintShown = false

    val shouldShowAccountHint = MutableStateFlow(false)

    init {
        observeItems()
    }

    fun bootstrap(
        deepLinkListId: String? = null,
        deepLinkInviteId: String? = null
    ) {

        viewModelScope.launch {

            val uid = authProvider.currentUserId() ?: return@launch

            // 🔥 Auth Status setzen
            isAnonymous.value = authProvider.isAnonymous()

            // 🔥 Start Sync
            firestoreListener.startListSync(uid)

            // 🔥 Lokale Listen beobachten
            observeLists()

            // ✅ NEU: Invite Flow statt direkt Join
            if (deepLinkListId != null) {

                _state.update {
                    it.copy(
                        inviteListIds = listOf(deepLinkListId),
                        showInviteDialog = true
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------
    // 🔥 AUTH GATE ENTRY
    // ------------------------------------------------------------

    fun createInviteAndShare(
    listIds: List<String>,
    skipProfileCheck: Boolean = false
    ) {
        viewModelScope.launch {

            if (isSharingInProgress) {
                Log.w("SHARE_DEBUG", "Share already in progress → skip")
                return@launch
            }

            Log.d("SHARE_DEBUG", "VM called with listIds=$listIds")

            pendingShareListIds = listIds

            if (!skipProfileCheck) {
                ensureAuthenticated {
                    createInviteAndShare(listIds, skipProfileCheck = true)
                }
                return@launch
            }

            isSharingInProgress = true

            try {

                val userName = authProvider.getDisplayName() ?: "Unbekannt"

                val inviteId = changeQueue.enqueue("createInvite") {

                    firestoreDataSource.createInvite(
                        listIds = listIds,
                        createdByName = userName
                    )
                }

                val link = "https://shopme-app.de/invite?inviteId=$inviteId"

                _shareEvent.tryEmit(link)

                pendingShareListIds = null

            } catch (e: Exception) {
                Log.e("SHARE", "Failed to create invite", e)
            } finally {
                isSharingInProgress = false
            }
        }
    }

    // ------------------------------------------------------------
    // 🔥 PROFILE FLOW
    // ------------------------------------------------------------

    fun saveProfile(firstName: String, lastName: String, email: String) {

        val fullName = "$firstName $lastName".trim()
        authProvider.updateDisplayName(fullName)

        Log.d("PROFILE", "Saved profile: $fullName")

        val shareListIds = pendingShareListIds
        val authAction = pendingAuthAction

        // ✅ RESET VOR Resume (entscheidend!)
        pendingShareListIds = null
        pendingAuthAction = null

        _state.update {
            it.copy(
                showProfileScreen = false,
                profileTriggeredByShare = false
            )
        }

        // ✅ PRIORITÄT: generischer Auth Resume
        if (authAction != null) {
            authAction.invoke()
            return
        }

        // ✅ Fallback: bestehender Share Flow bleibt erhalten
        if (shareListIds != null) {
            createInviteAndShare(shareListIds)
        }
    }

    fun dismissProfileScreen() {

        val triggeredByShare = _state.value.profileTriggeredByShare
        val listIds = pendingShareListIds

        // ✅ ALLES zurücksetzen
        pendingAuthAction = null
        pendingShareListIds = null

        _state.update {
            it.copy(
                showProfileScreen = false,
                profileTriggeredByShare = false
            )
        }

        if (triggeredByShare && listIds != null) {
            createInviteAndShare(
                listIds = listIds,
                skipProfileCheck = true
            )
        }
    }

    fun openProfileScreen() {

        _state.update {
            it.copy(
                showProfileScreen = true,
                profileTriggeredByShare = false
            )
        }
    }

    // ------------------------------------------------------------
    // REST UNCHANGED
    // ------------------------------------------------------------

    fun itemsForList(listId: String) =
        roomRepository.observeItems(listId)

    fun setCurrentList(listId: String) {
        _currentListId.value = listId
        _state.update { it.copy(activeListId = listId) }
    }

    val viewState: StateFlow<ShoppingViewState> =
        combine(state, showWelcomeDialog) { s, welcome ->

            val active = s.lists.firstOrNull { it.id == s.activeListId }

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
        _state.update { it.copy(screenMode = ShoppingScreenMode.Normal) }
    }

    fun dispatch(action: ShoppingAction) {
        val result =
            de.shopme.presentation.reducer.reduce(
                state = _state.value,
                screenMode = _state.value.screenMode,
                action = action
            )

        _state.value = result.state
        result.effects.forEach { handleEffect(it) }
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

    private fun handleEffect(effect: UIEffect) {
        when (effect) {
            is UIEffect.AddItem -> addItem(effect.name)
            is UIEffect.ToggleItem -> toggleItem(effect.item)
            is UIEffect.DeleteItem -> deleteItem(effect.item)
            is UIEffect.UpdateItem -> updateItem(effect.item, effect.newName)
            is UIEffect.ShowUndo -> {
                lastUndoAction = effect.action
                viewModelScope.launch { _effects.emit(effect) }
            }
            else -> {}
        }
    }

    private fun addItem(name: String) {

        if (name.isBlank()) return

        viewModelScope.launch {

            val normalized = quantityMapper.normalize(name)
            val category = categoryMapper.resolve(normalized)
            val listId = currentListId.value ?: return@launch

            val now = System.currentTimeMillis()

            val item = ShoppingItemEntity(
                id = UUID.randomUUID().toString(),
                listId = listId,
                name = normalized,
                quantity = 1,
                category = category,
                isChecked = true,
                deletedAt = null,
                createdAt = now,
                updatedAt = now
            )

            // 🔥 BESTEHEND
            roomRepository.addItem(item)

            // 🔥 NEU: Queue Event
            changeQueueDao.insert(
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "item",
                    entityId = item.id,
                    listId = listId,
                    operation = "CREATE",
                    payload = null,
                    createdAt = now,
                    state = "PENDING",
                    baseVersion = item.updatedAt
                )
            )
        }
    }

    private fun toggleItem(item: ShoppingItem) {

        viewModelScope.launch {

            val now = System.currentTimeMillis()

            val updated = item.toEntity().copy(
                isChecked = !item.isChecked,
                updatedAt = now
            )

            // 🔥 BESTEHEND
            roomRepository.updateItem(updated)

            // 🔥 NEU
            changeQueueDao.insert(
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "item",
                    entityId = updated.id,
                    listId = updated.listId,
                    operation = "UPDATE",
                    payload = null,
                    createdAt = now,
                    state = "PENDING",
                    baseVersion = item.updatedAt
                )
            )
        }
    }

    private fun deleteItem(item: ShoppingItem) {

        viewModelScope.launch {

            val now = System.currentTimeMillis()

            // 🔥 BESTEHEND
            roomRepository.updateItem(
                item.toEntity().copy(
                    deletedAt = now,
                    updatedAt = now
                )
            )

            // 🔥 NEU
            changeQueueDao.insert(
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "item",
                    entityId = item.id,
                    listId = item.listId,
                    operation = "DELETE",
                    payload = null,
                    createdAt = now,
                    state = "PENDING",
                    baseVersion = item.updatedAt
                )
            )
        }
    }

    // 🔥 NUR DIE NEUEN / FEHLENDEN FUNKTIONEN HINZUFÜGEN

// ------------------------------------------------------------
// 🔥 FIX: observeItems
// ------------------------------------------------------------

    private fun observeItems() {

        viewModelScope.launch {

            currentListId
                .filterNotNull()
                .collectLatest { listId ->

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

// ------------------------------------------------------------
// 🔥 FIX: updateItem
// ------------------------------------------------------------

    private fun updateItem(item: ShoppingItem, newName: String) {

        viewModelScope.launch {

            val now = System.currentTimeMillis()

            val updated = item.copy(
                name = newName,
                isChecked = true,
                updatedAt = now
            )

            // 🔥 BESTEHEND
            roomRepository.updateItem(updated.toEntity())

            // 🔥 NEU
            changeQueueDao.insert(
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "item",
                    entityId = updated.id,
                    listId = updated.listId,
                    operation = "UPDATE",
                    payload = null,
                    createdAt = now,
                    state = "PENDING",
                    baseVersion = item.updatedAt
                )
            )
        }
    }

// ------------------------------------------------------------
// 🔥 FIX: onShareClicked (FEHLTE)
// ------------------------------------------------------------

    fun onShareClicked(listIds: List<String>) {
        createInviteAndShare(listIds)
    }

// ------------------------------------------------------------
// 🔥 FIX: startGoogleSignIn
// ------------------------------------------------------------

    fun startGoogleSignIn() {
        viewModelScope.launch {
            _effects.emit(UIEffect.StartGoogleSignIn)
        }
    }

// ------------------------------------------------------------
// 🔥 FIX: acceptInvite
// ------------------------------------------------------------

    fun acceptInvite(listIds: List<String>) {

        viewModelScope.launch {

            if (_state.value.isJoining) {
                Log.w("INVITE", "Already joining → skip")
                return@launch
            }

            val uid = authProvider.currentUserId() ?: return@launch

            _state.update { it.copy(isJoining = true) }

            try {

                val now = System.currentTimeMillis()

                listIds.forEach { listId ->

                    val alreadyExists = state.value.lists.any { it.id == listId }
                    if (alreadyExists) {
                        Log.w("INVITE", "Already member of list $listId → skip")
                        return@forEach
                    }

                    // 🔥 NEU: Queue statt direkter Firestore Call
                    changeQueueDao.insert(
                        ChangeQueueEntity(
                            id = UUID.randomUUID().toString(),
                            entityType = "membership",
                            entityId = "${uid}_$listId",
                            listId = listId,
                            operation = "ADD",
                            payload = null,
                            createdAt = now,
                            state = "PENDING",
                            progress = 0f,
                            baseVersion = 0L
                        )
                    )

                    // 🔥 Sync sofort starten
                    syncCoordinator.startSingleListSync(listId)
                }

                _state.update {
                    it.copy(
                        isJoining = false,
                        inviteListIds = emptyList(),
                        showInviteDialog = false,
                        activeListId = listIds.firstOrNull()
                    )
                }

            } catch (e: Exception) {

                Log.e("INVITE", "Accept failed", e)

                _state.update {
                    it.copy(
                        isJoining = false,
                        inviteListIds = emptyList(),
                        showInviteDialog = false
                    )
                }
            }
        }
    }

// ------------------------------------------------------------
// 🔥 FIX: declineInvite
// ------------------------------------------------------------

    fun declineInvite() {

        _state.update {
            it.copy(
                inviteListIds = emptyList(),
                showInviteDialog = false
            )
        }
    }

// ------------------------------------------------------------
// 🔥 FIX: dismissWelcomeDialog
// ------------------------------------------------------------

    fun dismissWelcomeDialog() {
        _showWelcomeDialog.value = false
    }

// ------------------------------------------------------------
// 🔥 FIX: startMultiStoreCreation
// ------------------------------------------------------------

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

    fun onEvent(event: ShopEvent) {

        when (event) {

            is ShopEvent.List.UndoLastAction -> {
                handleUndo()
            }

            else -> {
                // aktuell keine weiteren Events benötigt
                Log.w("EVENT", "Unhandled event: $event")
            }
        }
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

    private fun observeLists() {

        viewModelScope.launch {

            roomRepository.observeLists()
                .collect { lists ->

                    val domainLists =
                        lists
                            .map { it.toDomain() }
                            .filter { it.name.isNotBlank() }

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

    fun joinList(listId: String, inviteId: String?) {

        Log.w("JOIN", "joinList() is deprecated – use acceptInvite() instead")

        viewModelScope.launch {

            try {

                val remoteList = firestoreDataSource.getListOnce(listId)

                remoteList?.let { list ->

                    val uid = authProvider.currentUserId() ?: return@let

                    val now = System.currentTimeMillis()

                    // 🔥 Queue statt direkter Membership
                    changeQueueDao.insert(
                        ChangeQueueEntity(
                            id = UUID.randomUUID().toString(),
                            entityType = "membership",
                            entityId = "${uid}_$listId",
                            listId = list.id,
                            operation = "ADD",
                            payload = null,
                            createdAt = now,
                            state = "PENDING",
                            progress = 0f,
                            baseVersion = 0L
                        )
                    )

                    listDao.upsert(list)

                    syncCoordinator.startSingleListSync(list.id)

                    setCurrentList(list.id)
                }

            } catch (e: Exception) {
                Log.e("JOIN", "Join failed", e)
            }
        }
    }

    fun acceptCurrentInvite() {
        val listIds = _state.value.inviteListIds
        if (listIds.isNotEmpty()) {
            acceptInvite(listIds)
        }
    }

    private fun ensureAuthenticated(action: () -> Unit) {

        if (authProvider.isAnonymous()) {

            Log.d("AUTH", "User anonymous → open profile gate")

            pendingAuthAction = action

            _state.update {
                it.copy(
                    showProfileScreen = true,
                    profileTriggeredByShare = false
                )
            }

            return
        }

        action()
    }
}