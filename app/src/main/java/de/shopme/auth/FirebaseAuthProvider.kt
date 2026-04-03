package de.shopme.data.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import de.shopme.domain.auth.AuthProvider
import kotlinx.coroutines.delay

class FirebaseAuthProvider : AuthProvider {

    private val auth = FirebaseAuth.getInstance()

    override suspend fun ensureAuthenticated() {

        if (auth.currentUser != null) return

        auth.signInAnonymously().await()

        // 🔥 Warten bis Firebase wirklich gesetzt ist
        var attempts = 0
        while (auth.currentUser == null && attempts < 10) {
            delay(100)
            attempts++
        }

        if (auth.currentUser == null) {
            throw IllegalStateException("Auth failed: user still null after signIn")
        }
    }

    override suspend fun linkWithGoogle(idToken: String): Result<Unit> {
        return try {

            val credential = com.google.firebase.auth.GoogleAuthProvider
                .getCredential(idToken, null)

            val user = FirebaseAuth.getInstance().currentUser
                ?: return Result.failure(IllegalStateException("No current user"))

            // 🔥 NEU: Prüfen ob schon verknüpft
            val alreadyLinked = user.providerData.any {
                it.providerId == com.google.firebase.auth.GoogleAuthProvider.PROVIDER_ID
            }

            if (alreadyLinked) {
                Log.d("AUTH", "Google already linked → skip linking")
                return Result.success(Unit)
            }

            user.linkWithCredential(credential).await()

            Result.success(Unit)

        } catch (e: Exception) {

            // 🔥 Fallback: falls Firebase trotzdem meckert
            if (e.message?.contains("already been linked") == true) {
                Log.d("AUTH", "Google already linked (exception fallback)")
                return Result.success(Unit)
            }

            Result.failure(e)
        }
    }

    override fun currentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")
    }

    override fun currentUserIdOrNull(): String? {
        return auth.currentUser?.uid
    }

    override fun isAnonymous(): Boolean {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        return user?.isAnonymous ?: true
    }
}