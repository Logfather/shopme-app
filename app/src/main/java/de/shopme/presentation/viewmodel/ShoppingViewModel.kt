package de.shopme.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
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
import de.shopme.domain.account.AccountDeletionManager
import de.shopme.domain.auth.AuthProvider
import de.shopme.domain.invite.InviteFlowHandler
import de.shopme.domain.item.ItemActionHandler
import de.shopme.domain.model.*
import de.shopme.domain.model.SyncStatus
import de.shopme.domain.service.CategoryMapper
import de.shopme.domain.service.QuantityMapper
import de.shopme.domain.service.SpeechItemParser
import de.shopme.domain.usecase.CreateListUseCase
import de.shopme.domain.usecase.DeleteListUseCase
import de.shopme.presentation.action.ShoppingAction
import de.shopme.presentation.effect.ShoppingEffectHandler
import de.shopme.presentation.effect.UIEffect
import de.shopme.presentation.event.ShopEvent
import de.shopme.presentation.reducer.reduce
import de.shopme.presentation.state.*
import de.shopme.presentation.undo.UndoAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.util.UUID
import kotlinx.coroutines.tasks.await

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
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
    private val changeQueueDao: ChangeQueueDao,
    private val authViewModel: AuthViewModel,
    private val accountDeletionManager: AccountDeletionManager,
) : ViewModel() {

    private val itemActionHandler = ItemActionHandler(
        roomRepository,
        changeQueueDao,
        quantityMapper,
        categoryMapper
    )

    private val inviteFlowHandler = InviteFlowHandler(
        firestoreDataSource,
        roomRepository
    )

    private val effectHandler = ShoppingEffectHandler(
        authProvider = authProvider,
        viewModel = this,
        scope = viewModelScope,
        itemActionHandler = itemActionHandler
    )

    // ============================================================
    // 🔥 UI FLAGS & DIALOG STATE
    // Zweck:
    // - Steuert UI Overlays, Dialoge und Trigger
    //
    // Stabilitätsrelevanz:
    // - Mittel → beeinflusst UX, aber keine Datenkonsistenz
    // ============================================================

    private val _showWelcomeDialog = MutableStateFlow(true)
    val showWelcomeDialog: StateFlow<Boolean> = _showWelcomeDialog.asStateFlow()

    private val _showAccountAction = MutableStateFlow(false)
    val showAccountAction = _showAccountAction.asStateFlow()

    private val _shareReturnTrigger = MutableStateFlow(0)
    val shareReturnTrigger = _shareReturnTrigger.asStateFlow()

    private val _profileSavedTrigger = MutableStateFlow(0)
    val profileSavedTrigger = _profileSavedTrigger.asStateFlow()

    var triggerShareAnimation by mutableStateOf(false)
        private set

    // ============================================================
    // 🔥 CORE STATE & UI STATE
    // Zweck:
    // - Zentrale State-Verwaltung für UI
    // - Grundlage für alle Reducer / ViewState Berechnungen
    //
    // Stabilitätsrelevanz:
    // - KRITISCH → falsche Änderungen führen zu UI Inkonsistenzen
    // ============================================================

    private val _state = MutableStateFlow(ShoppingState())
    val state: StateFlow<ShoppingState> = _state.asStateFlow()


    private val _effects = MutableSharedFlow<UIEffect>()
    val effects = _effects.asSharedFlow()


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


// ============================================================
// 🔥 AUTH & USER STATE
// Zweck:
// - Spiegelung des aktuellen Auth-Zustands
// - Datenquelle für Profil, Anzeige, Berechtigungen
//
// Stabilitätsrelevanz:
// - KRITISCH → steuert Zugriff, Sync und Ownership
// ============================================================

    private val _firstName = MutableStateFlow<String?>(null)
    val firstName = _firstName.asStateFlow()

    private val _lastName = MutableStateFlow<String?>(null)
    val lastName = _lastName.asStateFlow()

    private val _nickName = MutableStateFlow<String?>(null)
    val nickName = _nickName.asStateFlow()

    val isAnonymous: StateFlow<Boolean> = authViewModel.isAnonymous

    val isGoogleUser: StateFlow<Boolean> = authViewModel.isGoogleUser

    val email: StateFlow<String?> = authViewModel.email

    private var authListener: FirebaseAuth.AuthStateListener? = null

// ============================================================
// 🔥 LIST & NAVIGATION STATE
// Zweck:
// - Aktuell aktive Liste
// - Navigation innerhalb der App
//
// Stabilitätsrelevanz:
// - Hoch → falsche IDs führen zu falschen Daten
// ============================================================

    private val _currentListId = MutableStateFlow<String?>(null)
    val currentListId: StateFlow<String?> = _currentListId

    private var bufferedLists: List<ShoppingListEntity>? = null
    private var lastBootstrapUid: String? = null

// ============================================================
// 🔥 SHARE / INVITE FLOW STATE
// Zweck:
// - Steuerung des Invite- und Sharing-Flows
//
// Stabilitätsrelevanz:
// - Hoch → beeinflusst Netzwerk-Flow & UX Timing
// ============================================================

    private var isSharingInProgress: Boolean = false
    private var pendingShareListIds: List<String>? = null
    private var pendingInviteListId: String? = null

    private val _shareEvent = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val shareEvent = _shareEvent.asSharedFlow()

    private var shouldAnimateOnReturn = false



// ============================================================
// 🔥 AUTH FLOW CONTROL
// Zweck:
// - Deferred Aktionen nach Login / Reauth
//
// Stabilitätsrelevanz:
// - KRITISCH → verhindert verlorene Aktionen
// ============================================================

    private var pendingAuthAction: (() -> Unit)? = null

// ============================================================
// 🔥 PROFILE FLOW STATE
// Zweck:
// - Temporäre Speicherung von Profiländerungen
//
// Stabilitätsrelevanz:
// - Mittel → betrifft UX, nicht Kernlogik
// ============================================================

    private var pendingProfileUpdate: Quadruple<String, String?, String?, String?>? = null

// ============================================================
// 🔥 DELETE / OPERATION STATE
// Zweck:
// - Kontrolle kritischer Operationen (DeleteAll, Generation Tracking)
//
// Stabilitätsrelevanz:
// - KRITISCH → schützt vor Race Conditions & Inkonsistenzen
// ============================================================

    private var isDeleteAllRunning = false
    private var deleteGeneration = 0

// ============================================================
// 🔥 UNDO SYSTEM
// Zweck:
// - Wiederherstellung letzter Aktionen
//
// Stabilitätsrelevanz:
// - Mittel → UX Feature, aber wichtig für Vertrauen
// ============================================================

    private var lastUndoAction: UndoAction? = null

// ============================================================
// 🔥 ACCOUNT HINT / UX STATE
// Zweck:
// - Steuerung von UI-Hinweisen
//
// Stabilitätsrelevanz:
// - Niedrig
// ============================================================

    private var _accountHintShown = false
    val shouldShowAccountHint = MutableStateFlow(false)

// ============================================================
// 🔥 10. BOOTSTRAP & APP LIFECYCLE
// Zweck:
// - Einstiegspunkt der App
// - Initialisiert Sync, Listener und DeepLink Handling
//
// Stabilitätsrelevanz:
// - KRITISCH → falsche Reihenfolge = Sync / UI bricht
// ============================================================

    init {

        viewModelScope.launch {
            authProvider.observeAuthState().collect { uid ->

                if (uid != null) {
                    Log.d("AUTH", "User logged in → start sync")
                    startSync(uid)

                    dispatch(
                        ShoppingAction.LoadUserProfile(uid)
                    )

                } else {
                    Log.d("AUTH", "User logged out → stop sync")
                    stopSync()
                }
            }
        }

        observeItems()
        observeEffects()
    }

    fun itemsForList(listId: String) =
        roomRepository.observeItems(listId)

    fun bootstrap(
        deepLinkListId: String? = null,
        deepLinkInviteId: String? = null
    ) {

        viewModelScope.launch {

            updateAuthState()

            val uid = authProvider.currentUserId()

            if (lastBootstrapUid == uid) {
                Log.d("BOOT", "Skip bootstrap → same UID")
                return@launch
            }

            lastBootstrapUid = uid

            // 1. System Start
            //syncCoordinator.start()
            firestoreListener.startListSync(uid)

            observeLists()

            // 🔥 Invite Flow
            handleInviteFlow(deepLinkInviteId)

            if (deepLinkInviteId != null) return@launch

            // Fallback
            if (deepLinkListId != null) {
                _state.update {
                    it.copy(
                        inviteListIds = listOf(deepLinkListId),
                        showInviteDialog = true
                    )
                }
            }

            val userId = authProvider.getCurrentUserUidOrNull()

            if (userId != null) {
                dispatch(
                    ShoppingAction.LoadUserProfile(userId)
                )
            } else {
                Log.w("BOOT", "User not ready yet → skip profile load")
            }
        }
    }

    fun dispatch(
        action: ShoppingAction? = null,
        event: ShopEvent? = null
    ) {
        Log.d("SORT_DEBUG", "Dispatch action=$action event=$event")

        val result = reduce(
            state = _state.value,
            screenMode = _state.value.screenMode,
            action = action,
            event = event
        )

        _state.value = result.state

        viewModelScope.launch {
            result.effects.forEach {
                Log.d("EFFECT_DEBUG", "EMIT $it")
                _effects.emit(it)
            }
        }
    }

    fun onEvent(event: ShopEvent) {
        Log.d("VM_EVENT", "RECEIVED $event")
        dispatch(event = event)
    }

    private fun observeEffects() {
        Log.d("EFFECT_OBSERVER", "STARTED")

        viewModelScope.launch {
            effects.collect { effect ->
                Log.d("EFFECT_DEBUG", "COLLECT $effect")
                handleEffect(effect)
            }
        }
    }
//  Code zum späteren ändern
// TODO REMOVE AFTER REFACTOR
// Original logic moved to ShoppingEffectHandler
//    private fun handleEffect(effect: UIEffect) {
//
//        when (effect) {
//
//            is UIEffect.AddItem -> {
//                Log.d("EFFECT_DEBUG", "AddItem effect: ${effect.name}")
//
//                viewModelScope.launch {
//                    val listId = currentListId.value ?: return@launch
//                    itemActionHandler.addItem(effect.name, listId)
//                }
//            }
//
//            is UIEffect.UpdateItem -> {
//                viewModelScope.launch {
//                    itemActionHandler.updateItem(effect.item, effect.newName)
//                }
//            }
//
//            is UIEffect.DeleteItem -> {
//                viewModelScope.launch {
//                    itemActionHandler.deleteItem(effect.item)
//                }
//            }
//
//            is UIEffect.ToggleItem -> {
//                viewModelScope.launch {
//                    itemActionHandler.updateItemChecked(
//                        itemId = effect.itemId,
//                        newChecked = effect.newChecked
//                    )
//                }
//            }
//
//            is UIEffect.LoadUserProfile -> {
//                viewModelScope.launch {
//                    performLoadUserProfile(effect)
//                }
//            }
//
//            is UIEffect.UpdateUserProfile -> {
//                viewModelScope.launch {
//                    performUpdateUserProfile(effect)
//                }
//            }
//
//            is UIEffect.DeleteAccount -> {
//                viewModelScope.launch {
//
//                    val userId = authViewModel.authUser.value?.uid ?: return@launch
//
//                    performDeleteAccountFlow(
//                        userId = userId,
//                        getIdToken = {
//                            null
//                        }
//                    )
//                }
//            }
//
//            is UIEffect.UnlinkGoogle -> {
//                viewModelScope.launch {
//                    performUnlinkGoogle()
//                }
//            }
//
//            else -> {
//                Log.w("UI_EFFECT", "Unhandled effect: $effect")
//            }
//        }
//    }

    private fun handleEffect(effect: UIEffect) {
        effectHandler.handle(effect)
    }

// ============================================================
// 🔥 SYNC CONTROL (Teil von Lifecycle)
// Zweck:
// - Start/Stop der Synchronisation
//
// Stabilitätsrelevanz:
// - KRITISCH → falscher Zustand = Dateninkonsistenz
// ============================================================

    private fun startSync(uid: String) {
        Log.d("SYNC_INIT", "Start sync for uid=$uid")
        syncCoordinator.start()
    }

    private fun stopSync() {
        Log.d("SYNC_INIT", "Stop sync")
        firestoreListener.stop()
        syncCoordinator.stop()
    }

// ============================================================
// 🔥 3. LISTS (KERNLOGIK)
// Zweck:
// - Verwaltung aller Shopping Lists
// - Synchronisation zwischen DB, UI und Sync Layer
//
// Stabilitätsrelevanz:
// - EXTREM KRITISCH → zentrale Datenquelle der App
// - Fehler hier = Datenverlust / falsche UI / Sync-Probleme
// ============================================================

// ------------------------------------------------------------
// 🔥 Observe Lists (Realtime + Sorting + Delete Protection)
// ------------------------------------------------------------

    private fun observeLists() {

        viewModelScope.launch {

            roomRepository.observeLists()
                .collect { lists ->

                    // ============================================================
                    // 🔥 SORT BUFFER (verhindert Flackern während Sorting)
                    // ============================================================
                    if (_state.value.isSorting) {
                        Log.d("SORT_DEBUG", "Buffer emission during sorting")
                        bufferedLists = lists
                        return@collect
                    }

                    val currentState = _state.value
                    val isDeleting = currentState.isDeletingAll
                    val generationAtStart = currentState.deleteGeneration

                    val effectiveLists = bufferedLists ?: lists
                    bufferedLists = null

                    val domainLists = effectiveLists
                        .map { it.toDomain() }
                        .filter { it.name.isNotBlank() }
                        .sortedBy { it.name.lowercase() }

                    // ============================================================
                    // 🔥 DELETE FLOW CONTROL
                    // ============================================================
                    if (isDeleting) {

                        if (domainLists.isNotEmpty()) {
                            Log.d("LIST_BLOCK", "Skip intermediate size=${domainLists.size}")
                            return@collect
                        }

                        Log.d("LIST_FLOW", "DeleteAll finished")

                        _state.update {
                            it.copy(
                                isDeletingAll = false,
                                lists = emptyList(),
                                activeListId = null
                            )
                        }

                        _showWelcomeDialog.value = true
                        return@collect
                    }

                    // ============================================================
                    // 🔥 STALE SNAPSHOT PROTECTION
                    // ============================================================
                    if (generationAtStart != _state.value.deleteGeneration) {
                        Log.d("LIST_BLOCK", "Skip stale emission (generation mismatch)")
                        return@collect
                    }

                    // ============================================================
                    // 🔥 EMPTY STATE GUARD (KRITISCHER FIX)
                    // verhindert Navigation Jump bei kurzen Leerzuständen
                    // ============================================================
                    if (
                        domainLists.isEmpty() &&
                        currentState.activeListId != null &&
                        !currentState.isDeletingAll &&
                        !currentState.isSorting
                    ) {
                        Log.d("LIST_BLOCK", "Skip transient empty emission")
                        return@collect
                    }

                    // ============================================================
                    // 🔥 NORMAL FLOW
                    // ============================================================
                    _state.update { current ->

                        val validActiveId =
                            current.activeListId
                                ?.takeIf { id -> domainLists.any { it.id == id } }

                        val newActiveId =
                            validActiveId
                                ?: current.activeListId   // 🔥 KEY FIX
                                ?: domainLists.firstOrNull()?.id

                        current.copy(
                            lists = domainLists,
                            screenMode =
                                if (current.screenMode is ShoppingScreenMode.Loading)
                                    ShoppingScreenMode.MultiOverview
                                else
                                    current.screenMode,
                            activeListId = newActiveId
                        )
                    }

                    _showWelcomeDialog.value = domainLists.isEmpty()
                }
        }
    }

// ------------------------------------------------------------
// 🔥 Create Lists
// ------------------------------------------------------------

    fun createListFromStore(store: StoreType) {

        viewModelScope.launch {

            val list = ShoppingListEntity(
                id = UUID.randomUUID().toString(),
                name = store.displayName,
                ownerId = "", // wird im Sync gesetzt
                storeTypes = listOf(store),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            roomRepository.createList(list)
        }
    }

    fun createCustomList(name: String) {

        viewModelScope.launch {

            val list = ShoppingListEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                ownerId = "",
                storeTypes = emptyList(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            roomRepository.createList(list)
        }
    }


// ============================================================
// 🔥 LIST NAVIGATION
// Zweck:
// - Setzt die aktuell aktive Liste
// - Steuert UI + Item-Observer
//
// Stabilitätsrelevanz:
// - KRITISCH → falsche ID = falsche Daten im UI
// ============================================================

    fun setCurrentList(listId: String) {

        Log.d("LIST_FLOW", "setCurrentList = $listId")

        _currentListId.value = listId

        _state.update {
            it.copy(activeListId = listId)
        }

        // 🔥 NEU: Sync starten
        syncCoordinator.startSingleListSync(listId)
    }

// ------------------------------------------------------------
// 🔥 Delete Single List
// ------------------------------------------------------------

    fun deleteList(list: ShoppingList) {
        viewModelScope.launch {

            val snapshot = deleteListUseCase(list.id)

            val action = UndoAction.DeleteList(snapshot)
            lastUndoAction = action

            _effects.emit(
                UIEffect.ShowUndo(
                    message = ""
                )
            )
        }
    }

// ------------------------------------------------------------
// 🔥 Delete ALL Lists (kritischer Flow)
// ------------------------------------------------------------

    fun showDeleteAllConfirm() {
        _state.update { it.copy(showDeleteAllConfirm = true) }
    }

    fun dismissDeleteAllConfirm() {
        _state.update { it.copy(showDeleteAllConfirm = false) }
    }

    fun confirmDeleteAll() {

        _state.update {
            it.copy(
                showDeleteAllConfirm = false,
                isDeletingAll = true,
                deleteGeneration = it.deleteGeneration + 1
            )
        }

        dispatch(ShoppingAction.DeleteAllLists)
    }

    fun deleteAllLists() {

        viewModelScope.launch {

            val lists = listDao.observeListsOnce()

            // 🔥 Sync stoppen
            lists.forEach { list ->
                syncCoordinator.stopSingleListSync(list.id)
            }

            // 🔥 Lokal löschen
            lists.forEach { list ->
                roomRepository.deleteList(list.id)
            }
        }
    }

    fun onDeleteAllCompleted() {

        isDeleteAllRunning = false

        _state.update {
            it.copy(isDeletingAll = false)
        }
    }

// ------------------------------------------------------------
// 🔥 Sorting Flow (Multi-Store Creation)
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

    fun createListsWithSorting(
        stores: List<StoreType>,
        customLists: List<String>
    ) {
        viewModelScope.launch {

            dispatch(event = ShopEvent.List.StartSorting)

            yield()

            dispatch(event = ShopEvent.List.SetSortingPhase(SortingPhase.Preparing))

            val startTime = System.currentTimeMillis()
            val minDuration = 2000L

            try {
                stores.forEach { createListFromStore(it) }
                customLists.forEach { createCustomList(it) }

            } finally {
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = minDuration - elapsed

                if (remaining > 0) delay(remaining)

                dispatch(event = ShopEvent.List.FinishSorting)

                bufferedLists?.let { lists ->
                    Log.d("SORT_DEBUG", "Apply buffered lists after sorting")
                    bufferedLists = null

                    val domainLists = lists
                        .map { it.toDomain() }
                        .filter { it.name.isNotBlank() }
                        .sortedBy { it.name.lowercase() }

                    _state.update { current ->

                        val validActiveId =
                            current.activeListId
                                ?.takeIf { id -> domainLists.any { it.id == id } }

                        val newActiveId =
                            validActiveId ?: domainLists.firstOrNull()?.id

                        current.copy(
                            lists = domainLists,
                            screenMode =
                                if (current.screenMode is ShoppingScreenMode.MultiSelect)
                                    ShoppingScreenMode.MultiOverview
                                else
                                    current.screenMode,
                            activeListId = newActiveId
                        )
                    }

                    _showWelcomeDialog.value = domainLists.isEmpty()
                }
            }
        }
    }

// ============================================================
// 🔥 4. ITEMS
// Zweck:
// - CRUD für Shopping Items
// - Verbindung zwischen UI → DB → Sync Queue
//
// Stabilitätsrelevanz:
// - SEHR HOCH → falsche Updates führen zu Datenverlust / Inkonsistenz
// ============================================================

// ------------------------------------------------------------
// 🔥 Observe Items (Realtime + Sync Status)
// ------------------------------------------------------------

    private fun observeItems() {
        viewModelScope.launch {
            currentListId
                .filterNotNull()
                .collectLatest { listId ->

                    roomRepository
                        .observeItemsWithSyncStatus(listId)
                        .collect { itemsWithStatus ->

                            val domainItems = itemsWithStatus.map { (entity, statusEntity) ->
                                entity.toDomain().copy(
                                    syncStatus = statusEntity
                                )
                            }
                            _state.update {
                                it.copy(items = domainItems)
                            }
                        }
                }
        }
    }

// ------------------------------------------------------------
// 🔥 Share Flow Entry
// ------------------------------------------------------------

    fun onShareClicked(listIds: List<String>) {
        createInviteAndShare(listIds)
    }

// ------------------------------------------------------------
// 🔥 Create Invite + Share
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

            pendingShareListIds = listIds

            if (!skipProfileCheck) {

                ensureAuthenticated {
                    createInviteAndShare(listIds, skipProfileCheck = true)
                }

                return@launch
            }

            isSharingInProgress = true

            val startTime = System.currentTimeMillis()
            val minDuration = 1000L

            try {

                val userName = authProvider.getDisplayName() ?: "Unbekannt"

                val inviteId = changeQueue.enqueue("createInvite") {

                    firestoreDataSource.createInvite(
                        listIds = listIds,
                        createdByName = userName,
                        ownerId = authProvider.currentUserId()
                    )
                }

                val link = "https://shopme-app.de/invite?inviteId=$inviteId"

                _shareEvent.tryEmit(link)

                shouldAnimateOnReturn = true

                pendingShareListIds = null

            } catch (e: Exception) {
                Log.e("SHARE_DEBUG", "Failed to create invite", e)

            } finally {

                val elapsed = System.currentTimeMillis() - startTime
                val remaining = minDuration - elapsed

                if (remaining > 0) delay(remaining)

                isSharingInProgress = false
            }
        }
    }

// ------------------------------------------------------------
// 🔥 Invite Handling
// ------------------------------------------------------------

    fun acceptInvite(listIds: List<String>, inviteId: String?) {

        viewModelScope.launch {

            if (_state.value.isJoining) {
                Log.w("INVITE", "Already joining → skip")
                return@launch
            }

            val uid = authProvider.currentUserId() ?: return@launch

            _state.update { it.copy(isJoining = true) }

            try {

                listIds.forEach { listId ->

                    val alreadyExists = state.value.lists.any { it.id == listId }
                    if (alreadyExists) return@forEach

                    roomRepository.addMembership(
                        listId = listId,
                        userId = uid
                    )
                }

                if (inviteId != null) {
                    roomRepository.consumeInvite(inviteId)
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

    fun declineInvite() {
        _state.update {
            it.copy(
                inviteListIds = emptyList(),
                inviteSenderName = null,
                showInviteDialog = false,
                inviteError = null
            )
        }
    }

    internal suspend fun handleInviteFlow(deepLinkInviteId: String?) {

        if (deepLinkInviteId == null) return

        _state.update {
            it.copy(
                isInviteLoading = true,
                inviteError = null
            )
        }

        val inviteData = inviteFlowHandler.loadInvite(deepLinkInviteId)

        if (inviteData == null) {
            _state.update {
                it.copy(
                    isInviteLoading = false,
                    inviteError = "Einladung ungültig oder abgelaufen"
                )
            }
            return
        }

        if (isInviteExpired(inviteData.createdAt)) {
            _state.update {
                it.copy(
                    isInviteLoading = false,
                    inviteError = "Einladung ist abgelaufen"
                )
            }
            return
        }

        if (inviteData.consumedAt != null) {
            _state.update {
                it.copy(
                    isInviteLoading = false,
                    inviteError = "Einladung wurde bereits verwendet"
                )
            }
            return
        }

        val listIds = inviteData.listIds
        val senderName = inviteData.senderName

        _state.update {
            it.copy(
                isInviteLoading = false,
                inviteId = deepLinkInviteId,
                inviteListIds = listIds,
                inviteSenderName = senderName,
                showInviteDialog = true
            )
        }

        resolveInviteLists(listIds)
    }

// ============================================================
// 🔥 INVITE VALIDATION & RESOLUTION (HELPER)
// Zweck:
// - Validierung von Invite Daten (Expiry, Membership)
// - Auflösen der Listen für UI
//
// Stabilitätsrelevanz:
// - HOCH → entscheidet ob Join korrekt funktioniert
// ============================================================

    private fun isInviteExpired(createdAt: Long): Boolean {
        return inviteFlowHandler.isExpired(createdAt)
    }

    private fun resolveInviteLists(listIds: List<String>) {

        val currentLists = _state.value.lists

        val resolved = listIds.mapNotNull { id ->
            currentLists.find { it.id == id }
        }

        _state.update {
            it.copy(inviteResolvedLists = resolved)
        }

        // ============================================================
        // 🔥 JOIN COMPLETION CHECK (Realtime)
        // ============================================================

        checkJoinCompletion(resolved)
    }


// ============================================================
// 🔥 JOIN COMPLETION CHECK
// Zweck:
// - Erkennt automatisch, ob der User vollständig einer Einladung beigetreten ist
// - Schließt Dialog und reset UI State
//
// Stabilitätsrelevanz:
// - SEHR HOCH → verhindert hängenbleibende Invite-Zustände
// ============================================================

    private fun checkJoinCompletion(
        resolvedLists: List<ShoppingList>
    ) {
        val currentState = state.value

        val currentInviteIds = currentState.inviteListIds
        val currentInviteId = currentState.inviteId

        if (currentInviteIds.isEmpty() || currentInviteId == null) return

        val userId = authProvider.currentUserId() ?: return

        // 🔑 Bedingung 1: Alle Listen sind geladen
        val allListsPresent = currentInviteIds.all { inviteId ->
            resolvedLists.any { it.id == inviteId }
        }

        if (!allListsPresent) return

        // 🔑 Bedingung 2: Membership korrekt prüfen
        val userIsMemberOfAll = resolvedLists.all { list ->
            list.ownerId == userId || list.sharedWith.contains(userId)
        }

        if (!userIsMemberOfAll) return

        // ✅ JOIN ABGESCHLOSSEN

        _state.update {
            it.copy(
                isJoining = false,
                showInviteDialog = false,
                inviteId = null,
                inviteListIds = emptyList(),
                inviteResolvedLists = emptyList(),
                inviteSenderName = null,
                inviteError = null
            )
        }
    }

// ============================================================
// 🔥 7. UNDO SYSTEM
// Zweck:
// - Wiederherstellung letzter Aktionen
//
// Stabilitätsrelevanz:
// - Mittel → UX, aber wichtig für Vertrauen
// ============================================================

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
                is UndoAction.AddItem -> {
                    // aktuell kein Undo implementiert
                    // bewusst leer lassen
                }
            }

            lastUndoAction = null
        }
    }

// ============================================================
// 🔥 2. AUTH & USER
// Zweck:
// - Verwaltung des Auth-Zustands
// - Integration Firebase + Domain AuthProvider
//
// Stabilitätsrelevanz:
// - EXTREM KRITISCH → steuert Zugriff, Sync, Ownership
// ============================================================

// ------------------------------------------------------------
// 🔥 Auth State Sync
// ------------------------------------------------------------

    fun updateAuthState() {
        val user = FirebaseAuth.getInstance().currentUser
    }

    fun syncUserFromFirebase() {

        val uid = authProvider.getCurrentUserUidOrNull()

        val isGoogle = authProvider.isGoogleUser()

        if (uid == null) {
            _state.update {
                it.copy(
                    displayName = null,
                    hasProfile = false
                )
            }
            return
        }

        val fullName = authProvider.getDisplayName()
        val email = authProvider.getEmail()

        val firstName = fullName
            ?.substringBefore(" ")
            ?.replaceFirstChar { it.uppercase() }

        val lastName = fullName
            ?.substringAfter(" ", "")
            ?.replaceFirstChar { it.uppercase() }

        val fallbackName = email?.substringBefore("@")

        val finalDisplayName = when {
            !firstName.isNullOrBlank() -> firstName
            !fallbackName.isNullOrBlank() -> fallbackName
            else -> "Profil"
        }

        _firstName.value = firstName
        _lastName.value = lastName

        viewModelScope.launch {

            val safeFirstName = firstName ?: ""
            val safeLastName = lastName ?: ""
            val safeEmail = email ?: ""

            firestoreDataSource.upsertUserProfile(
                uid = uid,
                firstName = safeFirstName,
                lastName = safeLastName,
                email = safeEmail,
                profileName = null
            )
        }
    }

// ------------------------------------------------------------
// 🔥 Firebase Auth Listener
// ------------------------------------------------------------

    fun startAuthListener() {

        if (authListener != null) return

        authListener = FirebaseAuth.AuthStateListener { auth ->

            val user = auth.currentUser

            val isGoogle = user
                ?.providerData
                ?.any { it.providerId == "google.com" } == true


            val uid = user?.uid ?: return@AuthStateListener

            startUserProfileListener(uid)

            dispatch(
                ShoppingAction.LoadUserProfile(uid)
            )
        }
    }

    fun stopAuthListener() {
        authListener?.let {
            FirebaseAuth.getInstance().removeAuthStateListener(it)
        }
        authListener = null
    }

// ============================================================
// 🔥 USER PROFILE LISTENER (Realtime Firestore)
// Zweck:
// - Lauscht auf Änderungen im User-Profil
// - Synchronisiert UI automatisch
//
// Stabilitätsrelevanz:
// - HOCH → falsche Listener = veraltete UI oder Memory Leaks
// ============================================================

    private var userProfileListener: ListenerRegistration? = null

    private fun startUserProfileListener(uid: String) {

        userProfileListener?.remove()

        userProfileListener =
            firestoreDataSource.listenToUserProfile(uid) { data ->

                val profileName = data?.get("profileName") as? String
                val firstName = data?.get("firstName") as? String
                val lastName = data?.get("lastName") as? String
                val email = data?.get("email") as? String

                dispatch(
                    ShoppingAction.UserProfileLoaded(
                        uid = uid,
                        profileName = profileName,
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        exists = data != null
                    )
                )
            }
    }

// ============================================================
// 🔥 PROFILE FLOW
// Zweck:
// - Erstellung, Aktualisierung und Synchronisation von Profilen
//
// Stabilitätsrelevanz:
// - HOCH → beeinflusst Sharing, Ownership, UX
// ============================================================

// ------------------------------------------------------------
// 🔥 Profile UI Flow
// ------------------------------------------------------------

    fun onProfileCreated(
        firstName: String,
        lastName: String,
        email: String
    ) {
        viewModelScope.launch {

            try {
                saveProfileLocally(
                    firstName = firstName,
                    lastName = lastName,
                    email = email
                )

                dispatch(event = ShopEvent.System.OpenProfileScreen)

                val listIds = pendingShareListIds

                if (listIds != null) {
                    pendingShareListIds = null

                    createInviteAndShare(
                        listIds = listIds,
                        skipProfileCheck = true
                    )
                }

            } catch (e: Exception) {
                Log.e("PROFILE", "Failed to save profile", e)
            }
        }
    }

    fun onProfileCreated(
        firstName: String,
        lastName: String,
        email: String,
        nickName: String
    ) {
        viewModelScope.launch {

            // 🔥 1. Optimistic UI Update (wie im Original!)
            _state.update {
                it.copy(
                    displayName = nickName,
                    hasProfile = true,
                    showProfileScreen = false
                )
            }

            _nickName.value = nickName

            val uid = FirebaseAuth.getInstance().currentUser?.uid

            if (uid != null) {
                firestoreDataSource.upsertUserProfile(
                    uid = uid,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    profileName = nickName
                )
            }

            dispatch(event = ShopEvent.System.OpenProfileScreen)
        }
    }

    fun updateUserProfileUnified(
        nickName: String,
        firstName: String?,
        lastName: String?,
        email: String?
    ) {
        viewModelScope.launch {

            val current = _state.value

            val initialNick = current.displayName ?: ""
            val initialFirst = current.firstName ?: ""
            val initialLast = current.lastName ?: ""
            val initialEmail = current.email ?: ""

            val isOnlyProfileChange =
                nickName.trim() != initialNick.trim() &&
                        firstName.isNullOrBlank() &&
                        lastName.isNullOrBlank()

            if (isOnlyProfileChange) {

                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

                firestoreDataSource.upsertUserProfile(
                    uid = uid,
                    firstName = initialFirst,
                    lastName = initialLast,
                    email = initialEmail,
                    profileName = nickName
                )

                _state.update {
                    it.copy(
                        showProfileScreen = false,
                        profileTriggeredByShare = false
                    )
                }

                return@launch
            }

            pendingProfileUpdate = Quadruple(
                nickName,
                firstName,
                lastName,
                email
            )

            dispatch(event = ShopEvent.System.ShowSaveChoice)
        }
    }

    fun showSaveChoice(
        nickName: String,
        firstName: String?,
        lastName: String?,
        email: String?
    ) {
        pendingProfileUpdate = Quadruple(
            nickName,
            firstName,
            lastName,
            email
        )

        dispatch(event = ShopEvent.System.ShowSaveChoice)
    }

    fun hideSaveChoice() {
        dispatch(event = ShopEvent.System.HideSaveChoice)
    }

    fun confirmManualSave() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val data = pendingProfileUpdate ?: return

        viewModelScope.launch {

            firestoreDataSource.upsertUserProfile(
                uid = uid,
                firstName = data.second,
                lastName = data.third,
                email = data.fourth,
                profileName = data.first.trim()
            )

            pendingProfileUpdate = null

            _state.update {
                it.copy(
                    showSaveChoice = false,
                    showProfileScreen = false
                )
            }
        }
    }

    fun confirmGoogleSave() {

        val data = pendingProfileUpdate ?: return

        _nickName.value = data.first

        pendingProfileUpdate = null

        _state.update {
            it.copy(showSaveChoice = false)
        }

        startGoogleSignIn()
    }

    fun cancelProfileEditing() {
        viewModelScope.launch {

            dispatch(event = ShopEvent.System.HideSaveChoice)

            _state.update {
                it.copy(
                    showProfileScreen = false,
                    profileTriggeredByShare = false
                )
            }

            pendingProfileUpdate = null
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

    fun dismissProfileScreen() {

        val triggeredByShare = _state.value.profileTriggeredByShare
        val listIds = pendingShareListIds

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

    fun saveProfile(firstName: String, lastName: String, email: String) {

        val fullName = "$firstName $lastName".trim()
        authProvider.updateDisplayName(fullName)

        val shareListIds = pendingShareListIds
        val authAction = pendingAuthAction

        pendingShareListIds = null
        pendingAuthAction = null

        _state.update {
            it.copy(
                showProfileScreen = false,
                profileTriggeredByShare = false
            )
        }

        if (authAction != null) {
            authAction.invoke()
            return
        }

        if (shareListIds != null) {
            createInviteAndShare(shareListIds)
        }
    }

    private suspend fun saveProfileLocally(
        firstName: String,
        lastName: String,
        email: String
    ) {
        val uid = ensureFirebaseUser()

        firestoreDataSource.saveUserProfile(
            uid = uid,
            firstName = firstName,
            lastName = lastName,
            email = email
        )

        Log.d("PROFILE", "Profile saved for uid=$uid")
    }

    private suspend fun ensureFirebaseUser(): String {
        return authProvider.requireUserId()
    }

// ------------------------------------------------------------
// 🔥 Profile Data Handling
// ------------------------------------------------------------

    fun loadUserProfile() {
        viewModelScope.launch {

            val user = authProvider.getCurrentUser()
                ?: return@launch

            val uid = user.uid

            val data = firestoreDataSource.getUserProfile(uid)

            val profileName = data?.get("profileName") as? String

            if (!profileName.isNullOrBlank()) {
                _state.update {
                    it.copy(
                        displayName = profileName,
                        hasProfile = !profileName.isNullOrBlank()
                    )
                }
            }
        }
    }

    suspend fun performLoadUserProfile(effect: UIEffect.LoadUserProfile){
        try {
            val data = firestoreDataSource.getUserProfile(effect.uid)

            val profileName = data?.get("profileName") as? String
            val firstName = data?.get("firstName") as? String
            val lastName = data?.get("lastName") as? String
            val email = data?.get("email") as? String

            dispatch(
                ShoppingAction.UserProfileLoaded(
                    uid = effect.uid,
                    profileName = profileName,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    exists = data != null
                )
            )

        } catch (e: Exception) {

            Log.w("PROFILE", "User profile not accessible yet", e)

            dispatch(
                ShoppingAction.UserProfileLoaded(
                    uid = effect.uid,
                    profileName = null,
                    firstName = null,
                    lastName = null,
                    email = null,
                    exists = false
                )
            )
        }
    }

    suspend fun performUpdateUserProfile (effect: UIEffect.UpdateUserProfile){
        try {
            firestoreDataSource.upsertUserProfile(
                uid = effect.uid,
                firstName = effect.firstName,
                lastName = effect.lastName,
                email = effect.email,
                profileName = effect.nickName
            )

        } catch (e: Exception) {
            Log.e("PROFILE", "Update failed", e)
        }
    }

// ============================================================
// 🔥 ACCOUNT LIFECYCLE
// Zweck:
// - Verwaltung von Account Operationen (Delete, Unlink)
//
// Stabilitätsrelevanz:
// - EXTREM KRITISCH → betrifft Datenintegrität und Sicherheit
// ============================================================

// ------------------------------------------------------------
// 🔥 Delete Account Flow
// ------------------------------------------------------------

    fun deleteAccount() {
        viewModelScope.launch {
            Log.d("DELETE", "Trigger delete flow")
            _effects.emit(UIEffect.DeleteAccount)
        }
    }

    fun performDeleteAccountFlow(
        userId: String,
        getIdToken: suspend () -> String?
    ) {
        viewModelScope.launch {

            val result = accountDeletionManager
                .deleteAccountWithReauth(userId, getIdToken)

            if (result.isSuccess) {

                Log.d("DELETE", "Account deletion success")

                _state.value = ShoppingState()

                _showWelcomeDialog.value = true

            } else {

                Log.e("DELETE", "Account deletion failed", result.exceptionOrNull())

                _effects.emit(
                    UIEffect.ShowSnackbar("Löschen fehlgeschlagen")
                )
            }
        }
    }

// ------------------------------------------------------------
// 🔥 Google Unlink
// ------------------------------------------------------------

    fun unlinkGoogleAccount() {
        viewModelScope.launch {
            _effects.emit(UIEffect.UnlinkGoogle)
        }
    }

    suspend fun performUnlinkGoogle() {

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Log.w("AUTH", "Unlink failed → user is null")
            return
        }

        try {

            val providers = user.providerData.map { it.providerId }

            if (providers.size <= 1) {

                _effects.emit(
                    UIEffect.ShowSnackbar(
                        "Google kann nicht entfernt werden (letzte Anmeldemethode)"
                    )
                )
                return
            }

            user.unlink("google.com").await()

            _effects.emit(
                UIEffect.ShowSnackbar("Google Konto entfernt")
            )

        } catch (e: Exception) {

            Log.e("AUTH", "Unlink failed", e)

            _effects.emit(
                UIEffect.ShowSnackbar("Fehler beim Entfernen von Google")
            )
        }
    }

    suspend fun linkWithGoogle(idToken: String): Result<Unit> {
        return authViewModel.linkWithGoogle(idToken)
    }

    fun onGoogleSignInSuccess() {
        viewModelScope.launch {

            val uid = authProvider.getCurrentUserUidOrNull()
                ?: return@launch

            val email = authProvider.getDisplayName()

            val pending = pendingProfileUpdate

            val nickName = pending?.first ?: _nickName.value
            val firstName = pending?.second
            val lastName = pending?.third

            if (nickName != null) {

                firestoreDataSource.upsertUserProfile(
                    uid = uid,
                    firstName = firstName ?: "",
                    lastName = lastName ?: "",
                    email = email ?: "",
                    profileName = nickName
                )
            }

            val authAction = pendingAuthAction

            if (authAction != null) {
                pendingAuthAction = null
                authAction.invoke()
                return@launch
            }

            pendingProfileUpdate = null

            val listIds = pendingShareListIds

            if (listIds != null) {
                pendingShareListIds = null

                dispatch(event = ShopEvent.System.OpenProfileScreen)

                createInviteAndShare(
                    listIds = listIds,
                    skipProfileCheck = true
                )
            } else {
                dispatch(event = ShopEvent.System.OpenProfileScreen)
            }
        }
    }

// ============================================================
// 🔥 UTILITY / HELPER
// Zweck:
// - Kleine Hilfsfunktionen zur Steuerung von Flows
//
// Stabilitätsrelevanz:
// - Mittel → indirekter Einfluss auf UX und Ablauf
// ============================================================

    private fun emitUndo(message: String) {
        viewModelScope.launch {
            _effects.emit(UIEffect.ShowUndo(message))
        }
    }

    fun startGoogleSignIn() {
        viewModelScope.launch {
            _effects.emit(UIEffect.StartGoogleSignIn)
        }
    }

    fun editList(list: ShoppingList) {
        setCurrentList(list.id)
        _state.update { it.copy(screenMode = ShoppingScreenMode.Normal) }
    }

    fun acceptCurrentInvite() {

        val inviteId = _state.value.inviteId ?: return
        val listIds = _state.value.inviteListIds

        if (listIds.isEmpty()) return

        val resolved = _state.value.inviteResolvedLists

        if (resolved.isNullOrEmpty()) {
            _state.update {
                it.copy(
                    inviteError = "Listen noch nicht geladen"
                )
            }
            return
        }

        acceptInvite(listIds, inviteId)
    }

    fun dismissWelcomeDialog() {
        _showWelcomeDialog.value = false
    }
    private fun ensureAuthenticated(action: () -> Unit) {

        if (authProvider.isAnonymous()) {

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

    fun notifyReturnedFromShare() {
        if (!shouldAnimateOnReturn) return

        shouldAnimateOnReturn = false

        _shareReturnTrigger.value += 1
    }
}