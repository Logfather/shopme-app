package de.shopme.domain.account

import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.data.datasource.room.ListDao
import de.shopme.data.sync.SyncCoordinator
import de.shopme.data.sync.logging.RecoveryLog
import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.data.sync.queue.ChangeQueueDao
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
                        RecoveryLog.processError(
                            "Failed deleting owned list ${list.id}",
                            e
                        )
                    }
                }

                // MEMBER
                userId in list.sharedWith -> {
                    try {
                        firestore.removeUserFromList(list.id, userId)
                    } catch (e: Exception) {
                        RecoveryLog.processError(
                            "Failed removing membership ${list.id}",
                            e
                        )
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
    }

    suspend fun deleteAccount(userId: String) = withContext(Dispatchers.IO) {

        // ============================================================
        // 1. STOP SYNC (CRITICAL)
        // ============================================================

        RuntimeLog.account(
            "Account deletion started"
        )

        syncCoordinator.stop()

        // ============================================================
        // 2. CLEAR LOCAL QUEUE
        // ============================================================
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
                        RuntimeLog.account(
                            "Account deletion failed"
                        )
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
                        RuntimeLog.account(
                            "Account deletion failed"
                        )
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
    }

    suspend fun deleteAccountWithReauth(
        userId: String,
        getIdToken: suspend () -> String?
    ) = withContext(Dispatchers.IO) {

        // ============================================================
        // 1. STOP SYNC
        // ============================================================
        syncCoordinator.stop()

        // ============================================================
        // 2. FIRST TRY DELETE AUTH
        // ============================================================
        val firstDelete = tryDeleteAuth()

        if (firstDelete.isSuccess) {
            performDataCleanup(userId)
            return@withContext Result.success(Unit)
        }

        val error = firstDelete.exceptionOrNull()

        val requiresReauth =
            error?.message?.contains("requires recent login", ignoreCase = true) == true

        if (!requiresReauth) {
            RecoveryLog.processError(
                "Delete failed (no reauth possible)",
                error
            )
            return@withContext Result.failure(error ?: Exception("Unknown error"))
        }

        // ============================================================
        // 3. GET TOKEN FROM UI
        // ============================================================
        val token = getIdToken()

        if (token == null) {
            return@withContext Result.failure(Exception("Reauth cancelled"))
        }

        // ============================================================
        // 4. REAUTH
        // ============================================================
        val reauth = authViewModel.reauthenticateWithGoogle(token)

        if (reauth.isFailure) {
            RecoveryLog.processError(
                "Reauth failed",
                reauth.exceptionOrNull()
            )
            return@withContext reauth
        }

        // ============================================================
        // 5. RETRY DELETE
        // ============================================================
        val retryDelete = tryDeleteAuth()

        if (retryDelete.isFailure) {
            RecoveryLog.processError(
                "Retry delete failed",
                retryDelete.exceptionOrNull()
            )
            return@withContext retryDelete
        }

        performDataCleanup(userId)

        Result.success(Unit)
    }
}