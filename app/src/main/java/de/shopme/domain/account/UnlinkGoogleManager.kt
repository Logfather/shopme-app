package de.shopme.domain.account

import android.util.Log
import de.shopme.data.sync.SyncCoordinator
import de.shopme.domain.auth.AuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UnlinkGoogleManager(
    private val authProvider: AuthProvider,
    private val syncCoordinator: SyncCoordinator
) {

    suspend fun unlink(): Result<Unit> = withContext(Dispatchers.IO) {

        Log.d("UNLINK", "Start unlink process")

        // ============================================================
        // 1. STOP SYNC (safety)
        // ============================================================
        syncCoordinator.stop()

        val result = authProvider.unlinkGoogle()

        if (result.isSuccess) {
            Log.d("UNLINK", "Success")
        } else {
            Log.e("UNLINK", "Failed", result.exceptionOrNull())
        }

        result
    }

    suspend fun unlinkWithReauth(
        getIdToken: suspend () -> String?
    ): Result<Unit> = withContext(Dispatchers.IO) {

        Log.d("UNLINK", "Start unlink with reauth flow")

        // ============================================================
        // 1. STOP SYNC
        // ============================================================
        syncCoordinator.stop()

        // ============================================================
        // 2. FIRST TRY
        // ============================================================
        val firstAttempt = authProvider.unlinkGoogle()

        if (firstAttempt.isSuccess) {
            Log.d("UNLINK", "Success without reauth")
            return@withContext Result.success(Unit)
        }

        val error = firstAttempt.exceptionOrNull()

        // ============================================================
        // 3. CHECK IF REAUTH REQUIRED
        // ============================================================
        val requiresReauth =
            error?.message?.contains("requires recent login", ignoreCase = true) == true

        if (!requiresReauth) {
            Log.e("UNLINK", "Failed (no reauth possible)", error)
            return@withContext Result.failure(error ?: Exception("Unknown error"))
        }

        Log.d("UNLINK", "Reauth required → requesting token")

        // ============================================================
        // 4. REQUEST NEW TOKEN (UI)
        // ============================================================
        val idToken = getIdToken()

        if (idToken == null) {
            Log.e("UNLINK", "Reauth cancelled by user")
            return@withContext Result.failure(Exception("Reauth cancelled"))
        }

        // ============================================================
        // 5. REAUTHENTICATE
        // ============================================================
        val reauthResult = authProvider.reauthenticateWithGoogle(idToken)

        if (reauthResult.isFailure) {
            Log.e("UNLINK", "Reauth failed", reauthResult.exceptionOrNull())
            return@withContext reauthResult
        }

        Log.d("UNLINK", "Reauth success → retry unlink")

        // ============================================================
        // 6. RETRY UNLINK
        // ============================================================
        val retry = authProvider.unlinkGoogle()

        if (retry.isSuccess) {
            Log.d("UNLINK", "Success after reauth")
        } else {
            Log.e("UNLINK", "Retry failed", retry.exceptionOrNull())
        }

        retry
    }
}