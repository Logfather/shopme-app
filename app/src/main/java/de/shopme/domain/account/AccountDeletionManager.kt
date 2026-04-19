package de.shopme.domain.account

import android.util.Log
import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.data.datasource.room.ListDao
import de.shopme.data.sync.ChangeQueueDao
import de.shopme.data.sync.SyncCoordinator
import de.shopme.domain.auth.AuthProvider
import de.shopme.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccountDeletionManager(
    private val syncCoordinator: SyncCoordinator,
    private val listDao: ListDao,
    private val changeQueueDao: ChangeQueueDao,
    private val firestore: FirestoreGateway,
    private val authViewModel: AuthViewModel,
    private val authProvider: AuthProvider
) {

    private suspend fun tryDeleteAuth(): Result<Unit> {
        return authViewModel.deleteUser()
    }

    private suspend fun performDataCleanup(userId: String) {

        Log.d("ACCOUNT_DELETE", "Start data cleanup")

        // ============================================================
        // QUEUE
        // ============================================================
        changeQueueDao.clearAll()

        // ============================================================
        // LOCAL LISTS
        // ============================================================
        val lists = listDao.getAllListsOnce()

        lists.forEach { list ->

            when {
                // OWNER
                list.ownerId == userId -> {
                    try {
                        firestore.softDeleteList(list.id)
                    } catch (e: Exception) {
                        Log.e("ACCOUNT_DELETE", "Failed deleting owned list ${list.id}", e)
                    }
                }

                // MEMBER
                userId in list.sharedWith -> {
                    try {
                        firestore.removeUserFromList(list.id, userId)
                    } catch (e: Exception) {
                        Log.e("ACCOUNT_DELETE", "Failed removing membership ${list.id}", e)
                    }
                }
            }
        }

        // ============================================================
        // LOCAL DELETE
        // ============================================================
        lists.forEach {
            listDao.deleteById(it.id)
        }

        Log.d("ACCOUNT_DELETE", "Cleanup DONE")
    }

    suspend fun deleteAccount(userId: String) = withContext(Dispatchers.IO) {

        Log.d("ACCOUNT_DELETE", "START for user=$userId")

        // ============================================================
        // 1. STOP SYNC (CRITICAL)
        // ============================================================
        syncCoordinator.stop()

        // ============================================================
        // 2. CLEAR LOCAL QUEUE
        // ============================================================
        Log.d("ACCOUNT_DELETE", "Clearing queue")
        changeQueueDao.clearAll()

        // ============================================================
        // 3. LOAD LOCAL LISTS
        // ============================================================
        val lists = listDao.getAllListsOnce()

        lists.forEach { list ->

            when {
                // ====================================================
                // OWNER → DELETE REMOTE
                // ====================================================
                list.ownerId == userId -> {
                    try {
                        firestore.softDeleteList(list.id)
                    } catch (e: Exception) {
                        Log.e("ACCOUNT_DELETE", "Failed to delete owned list ${list.id}", e)
                    }
                }

                // ====================================================
                // MEMBER → REMOVE FROM sharedWith
                // ====================================================
                userId in list.sharedWith -> {
                    try {
                        firestore.removeUserFromList(
                            listId = list.id,
                            userId = userId
                        )
                    } catch (e: Exception) {
                        Log.e("ACCOUNT_DELETE", "Failed to remove membership ${list.id}", e)
                    }
                }
            }
        }

        // ============================================================
        // 4. CLEAR LOCAL DATA
        // ============================================================
        lists.forEach {
            listDao.deleteById(it.id)
        }

        Log.d("ACCOUNT_DELETE", "DONE")
    }

    suspend fun deleteAccountWithReauth(
        userId: String,
        getIdToken: suspend () -> String?
    ) = withContext(Dispatchers.IO) {

        Log.d("ACCOUNT_DELETE", "START with reauth for user=$userId")

        // ============================================================
        // 1. STOP SYNC
        // ============================================================
        syncCoordinator.stop()

        // ============================================================
        // 2. FIRST TRY DELETE AUTH
        // ============================================================
        val firstDelete = tryDeleteAuth()

        if (firstDelete.isSuccess) {
            Log.d("ACCOUNT_DELETE", "Auth delete success (no reauth)")
            performDataCleanup(userId)
            return@withContext Result.success(Unit)
        }

        val error = firstDelete.exceptionOrNull()

        val requiresReauth =
            error?.message?.contains("requires recent login", ignoreCase = true) == true

        if (!requiresReauth) {
            Log.e("ACCOUNT_DELETE", "Delete failed (no reauth possible)", error)
            return@withContext Result.failure(error ?: Exception("Unknown error"))
        }

        Log.d("ACCOUNT_DELETE", "Reauth required")

        // ============================================================
        // 3. GET TOKEN FROM UI
        // ============================================================
        val token = getIdToken()

        if (token == null) {
            Log.e("ACCOUNT_DELETE", "User cancelled reauth")
            return@withContext Result.failure(Exception("Reauth cancelled"))
        }

        // ============================================================
        // 4. REAUTH
        // ============================================================
        val reauth = authViewModel.reauthenticateWithGoogle(token)

        if (reauth.isFailure) {
            Log.e("ACCOUNT_DELETE", "Reauth failed", reauth.exceptionOrNull())
            return@withContext reauth
        }

        Log.d("ACCOUNT_DELETE", "Reauth success")

        // ============================================================
        // 5. RETRY DELETE
        // ============================================================
        val retryDelete = tryDeleteAuth()

        if (retryDelete.isFailure) {
            Log.e("ACCOUNT_DELETE", "Retry delete failed", retryDelete.exceptionOrNull())
            return@withContext retryDelete
        }

        Log.d("ACCOUNT_DELETE", "Delete success after reauth")

        performDataCleanup(userId)

        Result.success(Unit)
    }
}