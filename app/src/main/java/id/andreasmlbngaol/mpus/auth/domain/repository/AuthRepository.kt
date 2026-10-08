package id.andreasmlbngaol.mpus.auth.domain.repository

import id.andreasmlbngaol.mpus.auth.domain.model.LoginResult
import id.andreasmlbngaol.mpus.core.domain.model.User

/** The auth endpoints: create an account, sign in, and confirm an email. */
interface AuthRepository {
    suspend fun signup(email: String, password: String, username: String, nickname: String): LoginResult
    suspend fun login(email: String, password: String): LoginResult
    suspend fun logout()
    suspend fun verifyEmail(code: String): User
    suspend fun resendVerification(email: String): String
}
