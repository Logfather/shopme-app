package de.shopme.test.domain.account

import de.shopme.data.sync.SyncCoordinator
import de.shopme.domain.account.UnlinkGoogleManager
import de.shopme.domain.auth.AuthProvider
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class UnlinkGoogleManagerTest {

    private lateinit var authProvider: AuthProvider
    private lateinit var syncCoordinator: SyncCoordinator
    private lateinit var manager: UnlinkGoogleManager

    @Before
    fun setup() {
        authProvider = mockk(relaxed = true)
        syncCoordinator = mockk(relaxed = true)

        manager = UnlinkGoogleManager(authProvider, syncCoordinator)
    }

    @Test
    fun `unlink with reauth should retry and succeed`() = runBlocking {

        coEvery { authProvider.unlinkGoogle() } returnsMany listOf(
            Result.failure(Exception("requires recent login")),
            Result.success(Unit)
        )

        coEvery { authProvider.reauthenticateWithGoogle(any()) } returns Result.success(Unit)

        val result = manager.unlinkWithReauth {
            "valid_token"
        }

        assert(result.isSuccess)

        coVerify(exactly = 2) { authProvider.unlinkGoogle() }
    }
}