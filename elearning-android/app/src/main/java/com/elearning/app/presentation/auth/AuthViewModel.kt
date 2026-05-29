package com.elearning.app.presentation.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elearning.app.core.auth.GoogleAuthManager
import com.elearning.app.data.repository.PkceStore
import com.elearning.app.domain.model.AuthState
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.model.User
import com.elearning.app.domain.usecase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import javax.inject.Inject

// ─── UI State ─────────────────────────────────────────────────────────────────

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null,
    val authState: AuthState = AuthState.Loading
)

sealed class AuthEvent {
    data object NavigateToHome : AuthEvent()
    data object NavigateToLogin : AuthEvent()
    data class ShowError(val message: String) : AuthEvent()
    data class StartOAuthFlow(val authUri: String) : AuthEvent()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

/**
 * AuthViewModel — single ViewModel for all authentication screens.
 *
 * Exposes:
 *  - [uiState] as StateFlow for the UI layer.
 *  - [events] as SharedFlow for one-shot events (navigation, errors).
 *
 * All operations are performed in [viewModelScope] and route through UseCases.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val loginClassicUseCase: LoginClassicUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val registerUseCase: RegisterUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val checkAuthStateUseCase: CheckAuthStateUseCase,
    private val exchangeCodeForTokensUseCase: ExchangeCodeForTokensUseCase,
    private val authRepository: com.elearning.app.domain.repository.AuthRepository,
    private val authorizationService: AuthorizationService
) : ViewModel() {

    private val googleAuthManager = GoogleAuthManager(context)

    val googleSignInIntent: Intent
        get() = googleAuthManager.getSignInIntent()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    init {
        checkAuthState()
    }

    // ──────────────────────────── CHECK STATE ────────────────────────────────

    /**
     * Observes the persisted auth state on startup and routes accordingly.
     */
    fun checkAuthState() {
        Log.d("AuthViewModel", "Checking auth state...")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            checkAuthStateUseCase()
                .onEach { state ->
                    Log.d("AuthViewModel", "Auth state changed: $state")
                    _uiState.update { it.copy(authState = state, isLoading = false) }
                    when (state) {
                        AuthState.Authenticated -> {
                            Log.d("AuthViewModel", "User authenticated, fetching profile...")
                            fetchCurrentUser()
                            _events.emit(AuthEvent.NavigateToHome)
                        }
                        AuthState.Unauthenticated -> {
                            Log.d("AuthViewModel", "User unauthenticated, navigating to login")
                            _events.emit(AuthEvent.NavigateToLogin)
                        }
                        AuthState.Loading -> {
                            Log.d("AuthViewModel", "Auth state is Loading...")
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    // ──────────────────────────── CLASSIC LOGIN ──────────────────────────────

    fun loginClassic(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = loginClassicUseCase(email, password)) {
                is Result.Success -> {
                    fetchCurrentUser()
                    _events.emit(AuthEvent.NavigateToHome)
                }
                is Result.Error -> {
                    val msg = result.message ?: "Identifiants incorrects"
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                    _events.emit(AuthEvent.ShowError(msg))
                }
                Result.Loading -> Unit
            }
        }
    }

    // ──────────────────────────── OAUTH PKCE FLOWS ───────────────────────────

    /**
     * Builds the Google PKCE authorization request URI and emits it as an event
     * so the UI can launch the Custom Tab browser.
     */
    fun loginWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val authUri = authRepository.buildGoogleAuthRequest()
            _uiState.update { it.copy(isLoading = false) }
            _events.emit(AuthEvent.StartOAuthFlow(authUri))
        }
    }

    /**
     * Builds the Facebook PKCE authorization request URI.
     */
    fun loginWithFacebook() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val authUri = authRepository.buildFacebookAuthRequest()
            _uiState.update { it.copy(isLoading = false) }
            _events.emit(AuthEvent.StartOAuthFlow(authUri))
        }
    }

    /**
     * Called by the Activity after the OAuth2 redirect is received.
     * Handles both successful code exchange and error cases.
     */
    fun handleOAuthCallback(intent: Intent) {
        viewModelScope.launch {
            val response = AuthorizationResponse.fromIntent(intent)
            val exception = AuthorizationException.fromIntent(intent)

            when {
                exception != null -> {
                    val msg = exception.message ?: "OAuth2 error"
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                    _events.emit(AuthEvent.ShowError(msg))
                }
                response != null -> {
                    _uiState.update { it.copy(isLoading = true) }
                    val result = exchangeCodeForTokensUseCase(
                        code = response.authorizationCode ?: "",
                        codeVerifier = PkceStore.codeVerifier,
                        redirectUri = response.request.redirectUri.toString()
                    )
                    when (result) {
                        is Result.Success -> {
                            PkceStore.codeVerifier = ""  // clear after use
                            fetchCurrentUser()
                            _events.emit(AuthEvent.NavigateToHome)
                        }
                        is Result.Error -> {
                            val msg = result.message ?: "Échange de token échoué"
                            _uiState.update { it.copy(isLoading = false, error = msg) }
                            _events.emit(AuthEvent.ShowError(msg))
                        }
                        Result.Loading -> Unit
                    }
                }
            }
        }
    }

    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            googleAuthManager.handleSignInResult(data)
                .onSuccess { idToken ->
                    when (val result = authRepository.loginWithGoogle(idToken)) {
                        is Result.Success -> {
                            fetchCurrentUser()
                            _events.emit(AuthEvent.NavigateToHome)
                        }
                        is Result.Error -> {
                            val msg = result.message ?: "Erreur Google"
                            _uiState.update { it.copy(isLoading = false, error = msg) }
                            _events.emit(AuthEvent.ShowError(msg))
                        }
                        Result.Loading -> Unit
                    }
                }
                .onFailure { error ->
                    val msg = error.message ?: "Google Sign-In annulé"
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                    _events.emit(AuthEvent.ShowError(msg))
                }
        }
    }

    // ──────────────────────────── LOGOUT ─────────────────────────────────────

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            logoutUseCase()
            _uiState.update { AuthUiState() }   // reset all state
            _events.emit(AuthEvent.NavigateToLogin)
        }
    }

    // ──────────────────────────── REGISTER ───────────────────────────────────

    fun register(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = registerUseCase(email, password, firstName, lastName)) {
                is Result.Success -> {
                    // Auto-login after registration
                    loginClassic(email, password)
                }
                is Result.Error -> {
                    val msg = result.message ?: "Inscription échouée"
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                    _events.emit(AuthEvent.ShowError(msg))
                }
                Result.Loading -> Unit
            }
        }
    }

    // ──────────────────────────── FORGOT PASSWORD ────────────────────────────

    fun forgotPassword(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = forgotPasswordUseCase(email)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is Result.Error -> {
                    val msg = result.message ?: "Demande échouée"
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                    _events.emit(AuthEvent.ShowError(msg))
                }
                Result.Loading -> Unit
            }
        }
    }

    // ──────────────────────────── HELPERS ────────────────────────────────────

    private suspend fun fetchCurrentUser() {
        when (val result = getCurrentUserUseCase()) {
            is Result.Success ->
                _uiState.update { it.copy(currentUser = result.data, isLoading = false) }
            is Result.Error ->
                _uiState.update { it.copy(isLoading = false) }
            Result.Loading -> Unit
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        authorizationService.dispose()
    }
}
