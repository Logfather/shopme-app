package de.shopme.domain.auth

data class AuthUser(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val isAnonymous: Boolean,
    val isGoogleUser: Boolean
)