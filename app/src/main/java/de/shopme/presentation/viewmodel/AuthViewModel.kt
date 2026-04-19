package de.shopme.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.shopme.domain.auth.AuthProvider
import de.shopme.domain.auth.AuthUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authProvider: AuthProvider
) : ViewModel() {

    // ============================================================
    // 🔥 AUTH STATE
    // ============================================================

    private val _authUser = MutableStateFlow<AuthUser?>(null)
    val authUser: StateFlow<AuthUser?> = _authUser.asStateFlow()

    val isAnonymous: StateFlow<Boolean> =
        authUser.map { it?.isAnonymous ?: true }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isGoogleUser: StateFlow<Boolean> =
        authUser.map { it?.isGoogleUser ?: false }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val displayName: StateFlow<String?> =
        authUser.map { it?.displayName }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val email: StateFlow<String?> =
        authUser.map { it?.email }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ============================================================
    // 🔥 INIT
    // ============================================================

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authProvider.observeAuthState().collect {
                _authUser.value = authProvider.getCurrentUser()
            }
        }
    }

    // ============================================================
    // 🔥 ACTIONS
    // ============================================================

    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return authProvider.signInWithGoogle(idToken)
    }

    suspend fun linkWithGoogle(idToken: String): Result<Unit> {
        return authProvider.linkWithGoogle(idToken)
    }

    suspend fun unlinkGoogle(): Result<Unit> {
        return authProvider.unlinkGoogle()
    }

    suspend fun deleteUser(): Result<Unit> {
        return authProvider.deleteUser()
    }

    suspend fun reauthenticateWithGoogle(idToken: String): Result<Unit> {
        return authProvider.reauthenticateWithGoogle(idToken)
    }

    suspend fun signInAnonymously(): String {
        return authProvider.signInAnonymously()
    }

    fun updateDisplayName(name: String) {
        authProvider.updateDisplayName(name)
    }
}