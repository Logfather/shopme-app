package de.shopme.domain.auth

import com.google.firebase.auth.FirebaseAuth

interface AuthProvider {

    suspend fun linkWithGoogle(idToken: String): Result<Unit>

    fun isAnonymous(): Boolean

    suspend fun ensureAuthenticated()

    fun currentUserId(): String

    fun currentUserIdOrNull(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun getDisplayName(): String? {
        return FirebaseAuth.getInstance().currentUser?.displayName
    }

    fun updateDisplayName(name: String) {

        val user = FirebaseAuth.getInstance().currentUser ?: return

        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user.updateProfile(profileUpdates)
    }
}