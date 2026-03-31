package de.shopme.domain.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

interface AuthProvider {

    suspend fun linkWithGoogle(idToken: String): Result<Unit> {
        return try {

            val credential = com.google.firebase.auth.GoogleAuthProvider
                .getCredential(idToken, null)

            val user = FirebaseAuth.getInstance().currentUser
                ?: return Result.failure(IllegalStateException("No current user"))

            user.linkWithCredential(credential).await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isAnonymous(): Boolean

    suspend fun ensureAuthenticated()

    fun currentUserId(): String

    fun getCurrentUserId(): String?
}