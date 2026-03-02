package de.shopme.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import de.shopme.domain.auth.AuthProvider

class FirebaseAuthProvider : AuthProvider {

    private val auth = FirebaseAuth.getInstance()

    override suspend fun ensureAuthenticated() {

        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }

    override fun currentUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")
    }
}