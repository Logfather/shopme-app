package de.shopme.test.utils

import android.content.Context
import de.shopme.data.datasource.firestore.FirestoreDataSource
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.SyncCoordinator
import de.shopme.data.sync.FirestoreListener
import de.shopme.domain.auth.AuthProvider
import de.shopme.domain.service.CategoryMapper
import de.shopme.domain.service.QuantityMapper
import de.shopme.domain.service.SpeechItemParser
import de.shopme.domain.usecase.CreateListUseCase
import de.shopme.domain.usecase.DeleteListUseCase
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.core.network.NetworkMonitor
import de.shopme.data.sync.ChangeQueue
import de.shopme.domain.account.AccountDeletionManager
import de.shopme.domain.auth.AuthUser
import de.shopme.domain.model.InviteData
import de.shopme.presentation.viewmodel.AuthViewModel
import de.shopme.test.fakes.FakeChangeQueueDao
import de.shopme.test.fakes.FakeItemDao
import de.shopme.test.fakes.FakeListDao
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking

fun createTestViewModel(
    queueDao: FakeChangeQueueDao = FakeChangeQueueDao()
): ShoppingViewModel {

    val listDao = FakeListDao()
    val itemDao = FakeItemDao()

    // 🔥 ALLES mocken was nicht kritisch ist
    val firestoreDataSource = mockk<FirestoreDataSource>()

    coEvery { firestoreDataSource.getInviteData(any()) } returns null

    coEvery { firestoreDataSource.getInviteData("invite123") } returns
            InviteData(
                listIds = listOf("list1"),
                senderName = "Tester",
                createdAt = System.currentTimeMillis(),
                consumedAt = null
            )

    coEvery { firestoreDataSource.getInviteData("expired") } returns
            InviteData(
                listIds = listOf("list1"),
                senderName = "Tester",
                createdAt = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 10, // alt
                consumedAt = null
            )

    coEvery { firestoreDataSource.getInviteData("used") } returns
            InviteData(
                listIds = listOf("list1"),
                senderName = "Tester",
                createdAt = System.currentTimeMillis(),
                consumedAt = System.currentTimeMillis()
            )

    coEvery { firestoreDataSource.getInviteData("invalid") } returns null

    val syncCoordinator = mockk<SyncCoordinator>(relaxed = true)
    val firestoreListener = mockk<FirestoreListener>(relaxed = true)

    val repository = RoomShoppingRepository(
        itemDao = itemDao,
        listDao = listDao,
        changeQueueDao = queueDao,
        firestoreDataSource = firestoreDataSource
    )

    val createListUseCase = CreateListUseCase(repository)

    val deleteListUseCase = DeleteListUseCase(
        roomRepository = repository,
        firestore = firestoreDataSource
    )

    val authProvider = object : AuthProvider {

        override fun currentUserId(): String = "testUser"

        override fun getCurrentUserUidOrNull(): String? = "testUser"

        override suspend fun requireUserId(): String = "testUser" // 🔥 FIX

        override fun isAnonymous(): Boolean = false

        override fun isGoogleUser(): Boolean = true // 🔥 FIX

        override fun getDisplayName(): String? = "Test User"

        override fun getEmail(): String? = "test@shopme.com"

        override suspend fun linkWithGoogle(idToken: String) = Result.success(Unit)

        override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun signInAnonymously(): String {
            return "testUser"
        }

        override fun updateDisplayName(name: String) {}

        override suspend fun ensureAuthenticated() {}

        override fun observeAuthState(): Flow<String?> =
            flowOf("testUser") // 🔥 FIX (kein Firebase!)

        override suspend fun deleteUser(): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun reauthenticateWithGoogle(idToken: String): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun unlinkGoogle(): Result<Unit> {
            return Result.success(Unit)
        }

        override fun getCurrentUser(): AuthUser? {
            return AuthUser(
                uid = "testUser",
                isAnonymous = false,
                isGoogleUser = true,
                displayName = "Test User",
                email = "test@shopme.com"
            )
        }
    }

    // 🔥 NetworkMonitor NICHT selbst bauen → mock
    val networkMonitor = mockk<NetworkMonitor>(relaxed = true)

    val quantityMapper = mockk<QuantityMapper>(relaxed = true)
    val categoryMapper = mockk<CategoryMapper>(relaxed = true)
    val speechParser = mockk<SpeechItemParser>(relaxed = true)
    val changeQueue = mockk<ChangeQueue>(relaxed = true)
    val authViewModel = mockk<AuthViewModel>(relaxed = true)
    val accountDeletionManager = mockk<AccountDeletionManager>(relaxed = true)

    val vm = ShoppingViewModel(
        createListUseCase = createListUseCase,
        deleteListUseCase = deleteListUseCase,
        roomRepository = repository,
        quantityMapper = quantityMapper,
        categoryMapper = categoryMapper,
        networkMonitor = networkMonitor,
        authProvider = authProvider,
        speechItemParser = speechParser,
        firestoreDataSource = firestoreDataSource,
        itemDao = itemDao,
        listDao = listDao,
        firestoreListener = firestoreListener,
        changeQueue = changeQueue,
        syncCoordinator = syncCoordinator,
        changeQueueDao = queueDao,
        authViewModel = authViewModel,
        accountDeletionManager = accountDeletionManager
    )

    return vm
}