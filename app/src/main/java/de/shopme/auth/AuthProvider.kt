package de.shopme.domain.auth

interface AuthProvider {

    suspend fun ensureAuthenticated()

    fun currentUserId(): String
}