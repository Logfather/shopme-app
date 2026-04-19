package de.shopme.test.viewmodel

import de.shopme.domain.account.AccountDeletionManager
import de.shopme.domain.auth.AuthProvider
import de.shopme.domain.auth.AuthUser
import de.shopme.presentation.viewmodel.AuthViewModel
import de.shopme.presentation.viewmodel.ShoppingViewModel
import de.shopme.test.fakes.*
import de.shopme.test.utils.MainDispatcherRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    // ------------------------------------------------------------
    // 🧪 HELPER
    // ------------------------------------------------------------

    private fun createVM(user: AuthUser?): ShoppingViewModel {

        val authProvider = object : AuthProvider {

            override fun getCurrentUser(): AuthUser? = user

            override fun currentUserId(): String = user?.uid ?: "test"

            override fun getCurrentUserUidOrNull(): String? = user?.uid

            override fun isAnonymous(): Boolean = user?.isAnonymous ?: true

            override fun getDisplayName(): String? = user?.displayName

            override suspend fun signInWithGoogle(idToken: String) = Result.success(Unit)

            override suspend fun linkWithGoogle(idToken: String) = Result.success(Unit)

            override suspend fun signInAnonymously(): String = "test"

            override fun updateDisplayName(name: String) {}

            override suspend fun ensureAuthenticated() {}

            override fun getEmail(): String? = user?.email

            override fun isGoogleUser(): Boolean = user?.isGoogleUser ?: false

            override suspend fun requireUserId(): String = user?.uid ?: "test"

            override fun observeAuthState() = kotlinx.coroutines.flow.flowOf(user?.uid)

            override suspend fun deleteUser(): Result<Unit> {
                return Result.success(Unit)
            }

            override suspend fun reauthenticateWithGoogle(idToken: String): Result<Unit> {
                return Result.success(Unit)
            }

            override suspend fun unlinkGoogle(): Result<Unit> {
                return Result.success(Unit)
            }
        }

        val firestore = mockk<de.shopme.data.datasource.firestore.FirestoreDataSource>(relaxed = true)
        val authViewModel = mockk<AuthViewModel>(relaxed = true)
        val accountDeletionManager = mockk<AccountDeletionManager>(relaxed = true)

        return ShoppingViewModel(
            createListUseCase = mockk(relaxed = true),
            deleteListUseCase = mockk(relaxed = true),
            roomRepository = mockk(relaxed = true),
            quantityMapper = mockk(relaxed = true),
            categoryMapper = mockk(relaxed = true),
            networkMonitor = mockk(relaxed = true),
            authProvider = authProvider,
            speechItemParser = mockk(relaxed = true),
            firestoreDataSource = firestore,
            itemDao = FakeItemDao(),
            listDao = FakeListDao(),
            firestoreListener = mockk(relaxed = true),
            changeQueue = mockk(relaxed = true),
            syncCoordinator = mockk(relaxed = true),
            changeQueueDao = FakeChangeQueueDao(),

            // ✅ NEU (PFLICHT)
            authViewModel = authViewModel,
            accountDeletionManager = accountDeletionManager
        )
    }

    // ------------------------------------------------------------
    // 🧪 TESTS
    // ------------------------------------------------------------

    @Test
    fun google_user_name_extracted_correctly() = runTest {

        val vm = createVM(
            AuthUser(
                uid = "u1",
                displayName = "Max Mustermann",
                email = "max@test.de",
                isAnonymous = false,
                isGoogleUser = true
            )
        )

        vm.syncUserFromFirebase()

        val state = vm.state.value

        assertEquals("Max", state.displayName)
        assertFalse(vm.isAnonymous.value)
    }

    @Test
    fun anonymous_user_fallback_name() = runTest {

        val vm = createVM(
            AuthUser(
                uid = "u1",
                displayName = null,
                email = null,
                isAnonymous = true,
                isGoogleUser = false
            )
        )

        vm.syncUserFromFirebase()

        val state = vm.state.value

        assertEquals("Profil", state.displayName)
        assertTrue(vm.isAnonymous.value)
    }

    @Test
    fun email_fallback_name_from_email() = runTest {

        val vm = createVM(
            AuthUser(
                uid = "u1",
                displayName = null,
                email = "john@test.com",
                isAnonymous = false,
                isGoogleUser = false
            )
        )

        vm.syncUserFromFirebase()

        val state = vm.state.value

        assertEquals("john", state.displayName)
    }

    @Test
    fun `firestore upsert is called`() = runTest {

        val firestore = mockk<de.shopme.data.datasource.firestore.FirestoreDataSource>(relaxed = true)

        val user = AuthUser(
            uid = "u1",
            displayName = "Max Mustermann",
            email = "max@test.de",
            isAnonymous = false,
            isGoogleUser = true
        )

        val authProvider = mockk<AuthProvider>()
        val authViewModel = mockk<AuthViewModel>(relaxed = true)
        val accountDeletionManager = mockk<AccountDeletionManager>(relaxed = true)

        every { authProvider.getCurrentUser() } returns user
        every { authProvider.getCurrentUserUidOrNull() } returns "u1"
        every { authProvider.isAnonymous() } returns false
        every { authProvider.getDisplayName() } returns user.displayName
        every { authProvider.getEmail() } returns user.email
        every { authProvider.isGoogleUser() } returns true
        every { authProvider.observeAuthState() } returns kotlinx.coroutines.flow.flowOf("u1")
        coEvery { authProvider.requireUserId() } returns "u1"

        val vm = ShoppingViewModel(
            createListUseCase = mockk(relaxed = true),
            deleteListUseCase = mockk(relaxed = true),
            roomRepository = mockk(relaxed = true),
            quantityMapper = mockk(relaxed = true),
            categoryMapper = mockk(relaxed = true),
            networkMonitor = mockk(relaxed = true),
            authProvider = authProvider,
            speechItemParser = mockk(relaxed = true),
            firestoreDataSource = firestore,
            itemDao = FakeItemDao(),
            listDao = FakeListDao(),
            firestoreListener = mockk(relaxed = true),
            changeQueue = mockk(relaxed = true),
            syncCoordinator = mockk(relaxed = true),
            changeQueueDao = FakeChangeQueueDao(),
            authViewModel = authViewModel,
            accountDeletionManager = accountDeletionManager
        )

        vm.syncUserFromFirebase()

        coVerify {
            firestore.upsertUserProfile(
                uid = "u1",
                firstName = "Max",
                lastName = "Mustermann",
                email = "max@test.de",
                profileName = null
            )
        }
    }
}