package de.shopme.domain.auth

import kotlinx.coroutines.flow.Flow

interface AuthProvider {

    suspend fun linkWithGoogle(idToken: String): Result<Unit>

    suspend fun signInWithGoogle(idToken: String): Result<Unit>

    suspend fun signInAnonymously(): String

    suspend fun ensureAuthenticated()

    suspend fun requireUserId(): String

    suspend fun unlinkGoogle(): Result<Unit>

    suspend fun reauthenticateWithGoogle(idToken: String): Result<Unit>

    suspend fun deleteUser(): Result<Unit>

    fun currentUserId(): String

    fun getCurrentUserUidOrNull(): String?

    fun isAnonymous(): Boolean

    fun getDisplayName(): String?

    fun updateDisplayName(name: String)

    fun observeAuthState(): Flow<String?>

    fun getEmail(): String?

    fun isGoogleUser(): Boolean

    fun getCurrentUser(): AuthUser?
}