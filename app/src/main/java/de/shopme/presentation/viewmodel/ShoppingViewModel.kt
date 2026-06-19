package de.shopme.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.data.datasource.room.ListDao
import de.shopme.data.input.speech.SpeechItemParser
import de.shopme.data.mapper.EntityMapper.toDomain
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.data.sync.queue.ChangeQueue
import de.shopme.domain.account.AccountDeletionManager
import de.shopme.domain.auth.AuthProvider
import de.shopme.domain.invite.InviteFlowHandler
import de.shopme.domain.item.ItemActionHandler
import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.model.ShoppingList
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType
import de.shopme.domain.model.SyncOverview
import de.shopme.domain.service.CategoryMapper
import de.shopme.domain.service.QuantityMapper
import de.shopme.domain.usecase.DeleteListUseCase
import de.shopme.presentation.action.ShoppingAction
import de.shopme.presentation.effect.ShoppingEffectHandler
import de.shopme.presentation.effect.UIEffect
import de.shopme.presentation.event.ShopEvent
import de.shopme.presentation.reducer.reduce
import de.shopme.presentation.state.ShoppingScreenMode
import de.shopme.presentation.state.ShoppingState
import de.shopme.presentation.state.ShoppingViewState
import de.shopme.presentation.state.SortingPhase
import de.shopme.presentation.undo.UndoAction
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.yield
import java.util.UUID

class ShoppingViewModel(
    private val deleteListUseCase: DeleteListUseCase,
    private val roomRepository: RoomShoppingRepository,
    private val quantityMapper: QuantityMapper,
    private val categoryMapper: CategoryMapper,
    private val authProvider: AuthProvider,
    private val firestoreDataSource: FirestoreGateway,
    private val listDao: ListDao,
    private val changeQueue: ChangeQueue,
    private val authViewModel: AuthViewModel,
    private val accountDeletionManager: AccountDeletionManager,
    private val appContext: Context,
    private val speechItemParser: SpeechItemParser
) : ViewModel() {

    private var runtimeJob: Job? = null

    private val itemActionHandler = ItemActionHandler(
        roomRepository,
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
        itemActionHandler = itemActionHandler,
        firestoreGateway = firestoreDataSource,
        appContext = appContext,
        speechItemParser = speechItemParser
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

    private val _shareReturnTrigger = MutableStateFlow(0)
    val shareReturnTrigger = _shareReturnTrigger.asStateFlow()

    // ============================================================
    // 🔥 CORE STATE & UI STATE
    // Zweck:
    // - Zentrale State-Verwaltung für UI
    // - Grundlage für alle Reducer / ViewState Berechnungen
    //
    // Stabilitätsrelevanz:
    // - KRITISCH → falsche Änderungen führen zu UI Inkonsistenzen
    // ============================================================

    private val _syncOverview = MutableStateFlow(SyncOverview())

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

// ============================================================
// 🔥 LIST & NAVIGATION STATE
// Zweck:
// - Aktuell aktive Liste
// - Navigation innerhalb der App
//
// Stabilitätsrelevanz:
// - Hoch → falsche IDs führen zu falschen Daten
// ============================================================

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

    private var pendingProfileUpdate:
            PendingProfileUpdate? = null


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
// 🔥 10. BOOTSTRAP & APP LIFECYCLE
// Zweck:
// - Einstiegspunkt der App
// - Initialisiert Sync, Listener und DeepLink Handling
//
// Stabilitätsrelevanz:
// - KRITISCH → falsche Reihenfolge = Sync / UI bricht
// ============================================================

    init {
        observeAuthUser()
        startRuntimeObservers()
    }

    fun itemsForList(listId: String): Flow<List<ShoppingItem>> {
        return roomRepository.observeItems(listId)
            .map { entities ->

                entities
                    .map { it.toDomain() }
                    .groupBy { it.id }
                    .map { (_, list) ->
                        list
                            .sortedWith(
                                compareByDescending<ShoppingItem> { it.updatedAt }
                                    .thenByDescending { it.createdAt }
                            )
                            .first()
                    }
            }
    }

    fun bootstrap(
        deepLinkListId: String? = null,
        deepLinkInviteId: String? = null
    ) {

        viewModelScope.launch {

            val uid = authProvider.currentUserId()

            if (lastBootstrapUid == uid) {
                RuntimeLog.runtime(
                    "Skip bootstrap | same uid"
                )
                return@launch
            }

            lastBootstrapUid = uid

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
            RuntimeLog.runtime(
                "Skip profile load | handled by auth observer"
            )
        }
    }

    fun dispatch(
        action: ShoppingAction? = null,
        event: ShopEvent? = null
    ) {
        RuntimeLog.reducer(
            "Dispatch | action=$action event=$event screen=${_state.value.screenMode}"
        )

        val result = reduce(
            state = _state.value,
            action = action,
            event = event
        )

        _state.value = result.state

        viewModelScope.launch {
            result.effects.forEach {
                RuntimeLog.effect(
                    "Emit effect | effect=$it"
                )
                _effects.emit(it)
            }
        }
    }

    fun onEvent(event: ShopEvent) {
        RuntimeLog.reducer(
            "Receive event | event=$event"
        )
        dispatch(event = event)
    }

    private suspend fun observeEffects() {

        RuntimeLog.runtime(
            "Start effect collector"
        )

        effects.collect { effect ->

            RuntimeLog.effect(
                "Handle effect: $effect"
            )

            handleEffect(effect)
        }
    }

    private fun handleEffect(effect: UIEffect) {
        RuntimeLog.effect(
            "Handle effect | effect=$effect"
        )
        effectHandler.handle(effect)
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

    private suspend fun observeLists() {

        roomRepository.observeLists()
            .collectLatest { lists ->

                // ============================================================
                // 🔥 SORT BUFFER
                // verhindert Flackern während Sorting
                // ============================================================

                if (_state.value.isSorting) {

                    RuntimeLog.runtime(
                        "Buffer list emission during sorting"
                    )

                    bufferedLists = lists
                    return@collectLatest
                }

                val currentState = _state.value

                val effectiveLists =
                    bufferedLists ?: lists

                bufferedLists = null

                val domainLists =
                    effectiveLists
                        .map { it.toDomain() }
                        .filter { it.name.isNotBlank() }
                        .sortedBy { it.name.lowercase() }

                // ============================================================
                // 🔥 DELETE FLOW CONTROL
                // verhindert Zwischenzustände während Delete-All
                // ============================================================

                if (currentState.isDeletingAll) {

                    if (domainLists.isNotEmpty()) {

                        RuntimeLog.runtime(
                            "Skip intermediate delete emission size=${domainLists.size}"
                        )

                        return@collectLatest
                    }

                    RuntimeLog.runtime(
                        "DeleteAll completed"
                    )

                    _state.update {
                        it.copy(
                            isDeletingAll = false,
                            lists = emptyList(),
                            activeListId = null
                        )
                    }

                    _showWelcomeDialog.value = true

                    return@collectLatest
                }

                // ============================================================
                // 🔥 NORMAL FLOW
                // ============================================================

                RuntimeLog.runtime(
                    "observeLists emission size=${domainLists.size}"
                )

                _state.update { current ->

                    val validActiveId =
                        current.activeListId
                            ?.takeIf { activeId ->
                                domainLists.any { it.id == activeId }
                            }

                    val newActiveId =
                        validActiveId
                            ?: domainLists.firstOrNull()?.id

                    val nextScreen =
                        if (current.screenMode is ShoppingScreenMode.Loading) {
                            ShoppingScreenMode.MultiOverview
                        } else {
                            current.screenMode
                        }

                    RuntimeLog.runtime(
                        "Update screen=${current.screenMode} -> $nextScreen active=$newActiveId"
                    )

                    current.copy(
                        lists = domainLists,
                        screenMode = nextScreen,
                        activeListId = newActiveId
                    )
                }

                _showWelcomeDialog.value =
                    domainLists.isEmpty()
            }
    }

// ------------------------------------------------------------
// 🔥 Create Lists
// ------------------------------------------------------------

    fun createListFromStore(store: StoreType) {

        RuntimeLog.creation(
            "Create store list | store=${store.name}"
        )

        viewModelScope.launch {

            val list = ShoppingListEntity(
                id = UUID.randomUUID().toString(),
                name = store.displayName,
                ownerId = "",
                storeTypes = listOf(store),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            roomRepository.createList(list)
        }
    }

    fun createCustomList(name: String) {

        RuntimeLog.creation(
            "Create custom list | name=$name"
        )

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

        RuntimeLog.list(
            "Set current list | id=$listId"
        )

        _state.update {
            it.copy(activeListId = listId)
        }
    }

// ------------------------------------------------------------
// 🔥 Delete Single List
// ------------------------------------------------------------

    fun deleteList(list: ShoppingList) {
        viewModelScope.launch {

            val snapshot = deleteListUseCase(list.id)

            val action = UndoAction.DeleteList(snapshot)
            lastUndoAction = action
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
                isDeletingAll = true
            )
        }

        dispatch(ShoppingAction.DeleteAllLists)
    }

    fun deleteAllLists() {

        viewModelScope.launch {

            val lists = listDao.observeListsOnce()

            // 🔥 NUR lokale + Queue Operation
            lists.forEach { list ->
                roomRepository.deleteList(list.id)
            }
        }
    }

    fun onDeleteAllCompleted() {

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

    suspend fun createListsWithSorting(
        stores: List<StoreType>,
        customLists: List<String>
    ) {

        RuntimeLog.creation(
            "Start multi list creation | stores=${stores.size} custom=${customLists.size}"
        )

        dispatch(event = ShopEvent.List.StartSorting)

        yield()

        dispatch(event = ShopEvent.List.SetSortingPhase(SortingPhase.Preparing))

        val startTime = System.currentTimeMillis()
        val minDuration = 2000L

        try {

            stores.forEach {
                RuntimeLog.creation(
                    "Create store list | store=${it.name}"
                )
                createListFromStore(it)
            }

            customLists.forEach {

                RuntimeLog.creation(
                    "Create custom list | name=$it"
                )
                createCustomList(it)
            }

        } finally {
            val elapsed = System.currentTimeMillis() - startTime
            val remaining = minDuration - elapsed

            if (remaining > 0) delay(remaining)

            dispatch(event = ShopEvent.List.FinishSorting)

            bufferedLists?.let { lists ->
                RuntimeLog.runtime(
                    "Apply buffered lists after sorting"
                )
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

    private suspend fun observeItems() {

        state
            .map { it.activeListId }
            .distinctUntilChanged()
            .filterNotNull()
            .collectLatest { listId ->

                roomRepository
                    .observeItemsWithSyncStatus(listId)
                    .collect { itemsWithStatus ->

                        val domainItems =
                            itemsWithStatus.map { (entity, statusEntity) ->

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
                RuntimeLog.share(
                    "Skip share | already in progress"
                )
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
                RuntimeLog.share(
                    "Create invite failed"
                )

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
                RuntimeLog.invite(
                    "Skip join | already joining"
                )
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

                RuntimeLog.invite(
                    "Accept invite failed"
                )

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
// 🔥 2. AUTH & USER
// Zweck:
// - Verwaltung des Auth-Zustands
// - Integration Firebase + Domain AuthProvider
//
// Stabilitätsrelevanz:
// - EXTREM KRITISCH → steuert Zugriff, Sync, Ownership
// ============================================================


    fun syncUserFromFirebase() {

        val uid = authProvider.getCurrentUserUidOrNull()

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


        _firstName.value = firstName
        _lastName.value = lastName

        viewModelScope.launch {

            val safeFirstName = firstName ?: ""
            val safeLastName = lastName ?: ""
            val safeEmail = email ?: ""

            val existing = firestoreDataSource.getUserProfile(uid)

            val existingFirst =
                existing?.get("firstName") as? String ?: ""

            val existingLast =
                existing?.get("lastName") as? String ?: ""

            val existingEmail =
                existing?.get("email") as? String ?: ""

            val changed =
                existingFirst != safeFirstName ||
                        existingLast != safeLastName ||
                        existingEmail != safeEmail

            if (changed) {

                firestoreDataSource.upsertUserProfile(
                    uid = uid,
                    firstName = safeFirstName,
                    lastName = safeLastName,
                    email = safeEmail,
                    profileName = null
                )

                RuntimeLog.profile(
                    "Firestore profile updated"
                )

            } else {

                RuntimeLog.profile(
                    "Skip unchanged profile sync"
                )
            }
        }
    }

    private fun observeAuthUser() {

        viewModelScope.launch {

            authViewModel.authUser
                .map { it?.uid }
                .distinctUntilChanged()
                .collect { uid ->

                    if (uid != null) {

                        startUserProfileListener(uid)

                        RuntimeLog.runtime(
                            "User logged in"
                        )

                        if (runtimeJob == null || runtimeJob?.isCancelled == true) {

                            RuntimeLog.runtime(
                                "Restart runtime observers after login"
                            )

                            startRuntimeObservers()
                        }

                        dispatch(
                            ShoppingAction.LoadUserProfile(uid)
                        )
                    } else {

                        userProfileListener?.remove()
                        userProfileListener = null

                        RuntimeLog.runtime(
                            "User logged out"
                        )
                }
            }
        }
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

    private suspend fun observeSyncOverview() {

        roomRepository.observeSyncOverview()
            .collect { overview ->

                _syncOverview.value = overview

                RuntimeLog.sync(
                    "pending=${overview.pending} syncing=${overview.syncing} failed=${overview.failed}"
                )
            }
    }

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

                val listIds = pendingShareListIds

                if (listIds != null) {
                    pendingShareListIds = null

                    createInviteAndShare(
                        listIds = listIds,
                        skipProfileCheck = true
                    )
                }

            } catch (e: Exception) {
                RuntimeLog.profile(
                    "Save profile failed"
                )
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

            pendingProfileUpdate = PendingProfileUpdate(
                nickName = nickName,
                firstName = firstName,
                lastName = lastName,
                email = email
            )
        }
    }

    fun showSaveChoice(
        nickName: String,
        firstName: String?,
        lastName: String?,
        email: String?
    ) {
        pendingProfileUpdate = PendingProfileUpdate(
            nickName = nickName,
            firstName = firstName,
            lastName = lastName,
            email = email
        )

        onEvent(ShopEvent.System.ShowSaveChoice)
    }

    fun hideSaveChoice() {
        dispatch(event = ShopEvent.System.HideSaveChoice)
    }

    fun confirmManualSave() {

        RuntimeLog.profile(
            "Confirm manual profile save"
        )
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val data = pendingProfileUpdate ?: return

        viewModelScope.launch {

            firestoreDataSource.upsertUserProfile(
                uid = uid,
                firstName = data.firstName,
                lastName = data.lastName,
                email = data.email,
                profileName = data.nickName.trim()
            )

            // 🔥 NEU: Reducer informieren (entscheidend)
            onEvent(
                ShopEvent.System.ConfirmManualSave(
                    firstName = data.firstName,
                    lastName = data.lastName,
                    email = data.email,
                    nickName = data.nickName.trim()
                )
            )

            pendingProfileUpdate = null
        }
    }

    fun confirmGoogleSave() {

        RuntimeLog.profile(
            "Confirm google profile save"
        )

        val data = pendingProfileUpdate ?: return

        _nickName.value = data.nickName

        // 🔥 WICHTIG: pending NICHT löschen!
        // sonst verlierst du firstName/lastName
        // pendingProfileUpdate bleibt bestehen bis Success

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

        RuntimeLog.profile(
            "Profile saved locally"
        )
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

            RuntimeLog.profile(
                "User profile not accessible"
            )


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
            RuntimeLog.profile(
                "Update profile failed"
            )
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
            RuntimeLog.account(
                "Trigger account deletion"
            )
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

                RuntimeLog.account(
                    "Account deletion completed"
                )

                clearRuntimeState()

            } else {

                RuntimeLog.account(
                    "Account deletion failed"
                )

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
            RuntimeLog.account(
                "Unlink google failed | user null"
            )
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

            RuntimeLog.account(
                "Unlink google failed"
            )

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

            val nickName = pending?.nickName ?: _nickName.value
            val firstName = pending?.firstName
            val lastName = pending?.lastName

            if (nickName != null) {
                firestoreDataSource.upsertUserProfile(
                    uid = uid,
                    firstName = firstName ?: "",
                    lastName = lastName ?: "",
                    email = email ?: "",
                    profileName = nickName
                )
            }

            pendingProfileUpdate = null

            // 🔥 HIER gehört der Share hin
            val listIds = pendingShareListIds

            if (listIds != null) {
                pendingShareListIds = null

                val listId = listIds.firstOrNull()

                if (listId != null) {
                    RuntimeLog.share(
                        "Google sign in success | start sharing list=$listId"
                    )

                    dispatch(
                        event = ShopEvent.List.StartSharing(listId)
                    )
                }
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


    private fun clearRuntimeState() {

        // ------------------------------------------------------------
        // STOP RUNTIME
        // ------------------------------------------------------------

        runtimeJob?.cancel()
        runtimeJob = null

        // ------------------------------------------------------------
        // FIRESTORE LISTENER
        // ------------------------------------------------------------

        userProfileListener?.remove()
        userProfileListener = null

        // ------------------------------------------------------------
        // RUNTIME FLAGS
        // ------------------------------------------------------------

        pendingAuthAction = null
        pendingShareListIds = null
        pendingProfileUpdate = null
        lastUndoAction = null

        shouldAnimateOnReturn = false
        isSharingInProgress = false

        bufferedLists = null
        lastBootstrapUid = null

        // ------------------------------------------------------------
        // RESET STATE
        // ------------------------------------------------------------

        _showWelcomeDialog.value = true

        _state.value = ShoppingState()
    }

    private fun startRuntimeObservers() {

        runtimeJob?.cancel()

        runtimeJob = viewModelScope.launch {

            supervisorScope {

                launch { observeLists() }

                launch { observeItems() }

                launch { observeEffects() }

                launch { observeSyncOverview() }
            }
        }
    }

    fun getCurrentListId(): String? {
        return _state.value.activeListId
    }
}