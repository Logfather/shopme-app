package de.shopme.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import de.shopme.data.sync.logging.RecoveryLog
import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.domain.auth.AuthProvider
import de.shopme.domain.auth.AuthUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthProvider : AuthProvider {

    private val auth = FirebaseAuth.getInstance()

    // ============================================================
    // AUTH STATE
    // ============================================================

    override fun observeAuthState(): Flow<String?> = callbackFlow {

        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid)
        }

        FirebaseAuth.getInstance().addAuthStateListener(listener)

        awaitClose {
            FirebaseAuth.getInstance().removeAuthStateListener(listener)
        }
    }

    // ============================================================
    // USER
    // ============================================================

    override fun currentUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")
    }

    override fun getCurrentUserUidOrNull(): String? {
        return auth.currentUser?.uid
    }

    override fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    override fun isAnonymous(): Boolean {
        return auth.currentUser?.isAnonymous ?: true
    }

    override fun getDisplayName(): String? {
        return auth.currentUser?.displayName
    }

    override fun updateDisplayName(name: String) {

        val user = auth.currentUser ?: return

        val profileUpdates =
            com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()

        user.updateProfile(profileUpdates)
    }

    // ============================================================
    // AUTH ACTIONS
    // ============================================================

    override suspend fun ensureAuthenticated() {

        if (auth.currentUser != null) return

        RuntimeLog.auth(
            "Anonymous sign-in required"
        )

        auth.signInAnonymously().await()

        var attempts = 0
        while (auth.currentUser == null && attempts < 10) {
            delay(100)
            attempts++
        }

        if (auth.currentUser == null) {
            throw IllegalStateException("Auth failed: user still null")
        }
    }

    override suspend fun signInAnonymously(): String {

        val result = auth.signInAnonymously().await()

        val user = result.user
            ?: throw IllegalStateException("Anonymous sign-in failed")

        return user.uid
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {

            val credential = GoogleAuthProvider.getCredential(idToken, null)

            auth.signInWithCredential(credential).await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun linkWithGoogle(idToken: String): Result<Unit> {
        return try {

            val credential = GoogleAuthProvider.getCredential(idToken, null)

            val user = auth.currentUser
                ?: return Result.failure(IllegalStateException("No user"))

            val alreadyLinked = user.providerData.any {
                it.providerId == GoogleAuthProvider.PROVIDER_ID
            }

            if (alreadyLinked) {
                return Result.success(Unit)
            }

            user.linkWithCredential(credential).await()

            Result.success(Unit)

        } catch (e: Exception) {

            if (e.message?.contains("already been linked") == true) {
                return Result.success(Unit)
            }

            Result.failure(e)
        }
    }

    override fun getEmail(): String? {
        return auth.currentUser?.email
    }

    override fun isGoogleUser(): Boolean {
        return auth.currentUser
            ?.providerData
            ?.any { it.providerId == "google.com" } == true
    }

    override suspend fun requireUserId(): String {
        return getCurrentUserUidOrNull()
            ?: signInAnonymously()
    }



    override fun getCurrentUser(): AuthUser? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null

        val isGoogle = user.providerData.any {
            it.providerId == "google.com"
        }

        return AuthUser(
            uid = user.uid,
            displayName = user.displayName,
            email = user.email,
            isAnonymous = user.isAnonymous,
            isGoogleUser = isGoogle
        )
    }

    override suspend fun unlinkGoogle(): Result<Unit> {
        return try {

            val user = auth.currentUser
                ?: return Result.failure(IllegalStateException("No user"))

            val providers = user.providerData.map { it.providerId }

            val hasGoogle = providers.contains(GoogleAuthProvider.PROVIDER_ID)

            if (!hasGoogle) {
                return Result.success(Unit)
            }

            // 🔴 CRITICAL: letzter Provider?
            val realProviders = providers.filter { it != "firebase" }

            if (realProviders.size <= 1) {
                return Result.failure(
                    IllegalStateException("Cannot unlink last provider")
                )
            }

            user.unlink(GoogleAuthProvider.PROVIDER_ID).await()

            Result.success(Unit)

        } catch (e: Exception) {

            RecoveryLog.processError(
                "unlinkGoogle failed",
                e
            )

            Result.failure(e)
        }
    }

    override suspend fun reauthenticateWithGoogle(idToken: String): Result<Unit> {
        return try {

            val user = auth.currentUser
                ?: return Result.failure(IllegalStateException("No user"))

            val credential = GoogleAuthProvider.getCredential(idToken, null)

            user.reauthenticate(credential).await()

            Result.success(Unit)

        } catch (e: Exception) {

            RecoveryLog.processError(
                "Reauthentication failed",
                e
            )

            Result.failure(e)
        }
    }

    override suspend fun deleteUser(): Result<Unit> {
        return try {

            val user = auth.currentUser
                ?: return Result.failure(IllegalStateException("No user"))

            user.delete().await()

            Result.success(Unit)

        } catch (e: Exception) {

            RecoveryLog.processError(
                "deleteUser failed",
                e
            )

            Result.failure(e)
        }
    }
}