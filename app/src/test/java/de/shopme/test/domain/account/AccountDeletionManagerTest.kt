package de.shopme.test.domain.account

import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.data.datasource.room.ListDao
import de.shopme.data.sync.ChangeQueueDao
import de.shopme.data.sync.SyncCoordinator
import de.shopme.domain.account.AccountDeletionManager
import de.shopme.domain.auth.AuthProvider
import de.shopme.presentation.viewmodel.AuthViewModel
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class AccountDeletionManagerTest {

    private lateinit var syncCoordinator: SyncCoordinator
    private lateinit var listDao: ListDao
    private lateinit var changeQueueDao: ChangeQueueDao
    private lateinit var firestore: FirestoreGateway
    private lateinit var authProvider: AuthProvider
    private lateinit var manager: AccountDeletionManager
    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setup() {
        syncCoordinator = mockk(relaxed = true)
        listDao = mockk(relaxed = true)
        changeQueueDao = mockk(relaxed = true)
        firestore = mockk(relaxed = true)
        authProvider = mockk(relaxed = true)
        authViewModel = mockk(relaxed = true)

        manager = AccountDeletionManager(
            syncCoordinator,
            listDao,
            changeQueueDao,
            firestore,
            authViewModel,
            authProvider
        )
    }

    @Test
    fun `delete account without reauth should stop sync and cleanup`() = runBlocking {

        val userId = "user1"

        coEvery { authProvider.deleteUser() } returns Result.success(Unit)
        coEvery { listDao.getAllListsOnce() } returns emptyList()

        val result = manager.deleteAccountWithReauth(
            userId = userId,
            getIdToken = { null }
        )

        assert(result.isSuccess)

        verify { syncCoordinator.stop() }
        coVerify { changeQueueDao.clearAll() }
    }
    @Test
    fun `delete account with reauth should retry and succeed`() = runBlocking {

        val userId = "user1"

        coEvery { authProvider.deleteUser() } returnsMany listOf(
            Result.failure(Exception("requires recent login")),
            Result.success(Unit)
        )

        coEvery { authProvider.reauthenticateWithGoogle(any()) } returns Result.success(Unit)
        coEvery { listDao.getAllListsOnce() } returns emptyList()

        val result = manager.deleteAccountWithReauth(
            userId = userId,
            getIdToken = { "valid_token" }
        )

        assert(result.isSuccess)

        coVerify(exactly = 2) { authProvider.deleteUser() }
        coVerify { authProvider.reauthenticateWithGoogle("valid_token") }
    }
    @Test
    fun `delete account should fail if reauth cancelled`() = runBlocking {

        val userId = "user1"

        coEvery { authProvider.deleteUser() } returns Result.failure(Exception("requires recent login"))

        val result = manager.deleteAccountWithReauth(
            userId = userId,
            getIdToken = { null }
        )

        assert(result.isFailure)
    }
}