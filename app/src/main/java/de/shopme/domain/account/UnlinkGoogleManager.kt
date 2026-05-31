package de.shopme.domain.account

import de.shopme.data.sync.SyncCoordinator
import de.shopme.data.sync.logging.RecoveryLog
import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.domain.auth.AuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UnlinkGoogleManager(
    private val authProvider: AuthProvider,
    private val syncCoordinator: SyncCoordinator
) {

    suspend fun unlink(): Result<Unit> =
        withContext(Dispatchers.IO) {

            RuntimeLog.account(
                "Google unlink started"
            )

            syncCoordinator.stop()

            authProvider.unlinkGoogle()
        }

    suspend fun unlinkWithReauth(
        getIdToken: suspend () -> String?
    ): Result<Unit> = withContext(Dispatchers.IO) {

        // ============================================================
        // 1. STOP SYNC
        // ============================================================
        syncCoordinator.stop()

        // ============================================================
        // 2. FIRST ATTEMPT
        // ============================================================
        val firstAttempt =
            authProvider.unlinkGoogle()

        if (firstAttempt.isSuccess) {
            return@withContext Result.success(Unit)
        }

        val error =
            firstAttempt.exceptionOrNull()

        // ============================================================
        // 3. CHECK REAUTH REQUIREMENT
        // ============================================================
        val requiresReauth =
            error
                ?.message
                ?.contains(
                    "requires recent login",
                    ignoreCase = true
                ) == true

        if (!requiresReauth) {

            RecoveryLog.processError(
                "unlinkGoogle failed (no reauth possible)",
                error
            )

            return@withContext Result.failure(
                error ?: Exception("Unknown error")
            )
        }

        // ============================================================
        // 4. REQUEST TOKEN
        // ============================================================
        val idToken =
            getIdToken()

        if (idToken == null) {

            return@withContext Result.failure(
                Exception("Reauth cancelled")
            )
        }

        // ============================================================
        // 5. REAUTHENTICATE
        // ============================================================
        val reauthResult =
            authProvider
                .reauthenticateWithGoogle(
                    idToken
                )

        if (reauthResult.isFailure) {

            RecoveryLog.processError(
                "unlinkGoogle reauth failed",
                reauthResult.exceptionOrNull()
            )

            return@withContext reauthResult
        }

        // ============================================================
        // 6. RETRY UNLINK
        // ============================================================

        val retryResult =
            authProvider.unlinkGoogle()

        if (retryResult.isFailure) {

            RecoveryLog.processError(
                "unlinkGoogle retry failed",
                retryResult.exceptionOrNull()
            )
        }

        retryResult
    }
}