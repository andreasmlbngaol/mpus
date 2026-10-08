package id.andreasmlbngaol.mpus.auth.domain.usecase

import id.andreasmlbngaol.mpus.auth.domain.model.LoginResult
import id.andreasmlbngaol.mpus.auth.domain.repository.AuthRepository
import id.andreasmlbngaol.mpus.core.domain.model.User

/** Everything the auth screens do: sign up/in, verify an email, sign out. */
class AuthUseCase(private val auth: AuthRepository) {
    suspend fun login(email: String, password: String): LoginResult = auth.login(email, password)

    suspend fun signup(email: String, password: String, username: String, nickname: String): LoginResult =
        auth.signup(email, password, username, nickname)

    suspend fun logout() = auth.logout()

    suspend fun verifyEmail(code: String): User = auth.verifyEmail(code)

    suspend fun resendVerification(email: String): String = auth.resendVerification(email)
}
