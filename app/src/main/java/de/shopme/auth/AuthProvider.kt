package de.shopme.domain.auth

interface AuthProvider {

    suspend fun linkWithGoogle(idToken: String): Result<Unit>

    fun isAnonymous(): Boolean

    suspend fun ensureAuthenticated()

    fun currentUserId(): String

    fun currentUserIdOrNull(): String?
}