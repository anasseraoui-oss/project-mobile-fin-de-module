package com.elearning.app.domain.usecase.auth

import com.elearning.app.domain.model.AuthTokens
import com.elearning.app.domain.model.Result
import com.elearning.app.domain.repository.AuthRepository
import javax.inject.Inject

class LoginClassicUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthTokens> {
        if (email.isBlank()) return Result.Error(IllegalArgumentException("Email requis"))
        if (password.length < 6) return Result.Error(IllegalArgumentException("Mot de passe trop court"))
        return authRepository.loginClassic(email, password)
    }
}

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.logout()
}

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<Unit> {
        if (email.isBlank()) return Result.Error(IllegalArgumentException("Email requis"))
        if (password.length < 8) return Result.Error(IllegalArgumentException("Mot de passe: 8 caractères minimum"))
        if (firstName.isBlank()) return Result.Error(IllegalArgumentException("Prénom requis"))
        if (lastName.isBlank()) return Result.Error(IllegalArgumentException("Nom requis"))
        return authRepository.register(email, password, firstName, lastName)
    }
}

class ForgotPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) return Result.Error(IllegalArgumentException("Email requis"))
        return authRepository.forgotPassword(email)
    }
}

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.getCurrentUser()
}

class CheckAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke() = authRepository.observeAuthState()
}

class ExchangeCodeForTokensUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        code: String,
        codeVerifier: String,
        redirectUri: String
    ) = authRepository.exchangeCodeForTokens(code, codeVerifier, redirectUri)
}
