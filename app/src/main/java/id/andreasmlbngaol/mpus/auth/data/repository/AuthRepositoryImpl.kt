package id.andreasmlbngaol.mpus.auth.data.repository

import id.andreasmlbngaol.mpus.auth.data.source.AuthRemoteSource
import id.andreasmlbngaol.mpus.auth.domain.model.LoginResult
import id.andreasmlbngaol.mpus.auth.domain.repository.AuthRepository
import id.andreasmlbngaol.mpus.core.data.mapper.toDomain
import id.andreasmlbngaol.mpus.core.domain.model.User

class AuthRepositoryImpl(private val source: AuthRemoteSource) : AuthRepository {

    override suspend fun signup(
        email: String,
        password: String,
        username: String,
        nickname: String,
    ): LoginResult {
        val dto = source.signup(email, password, username, nickname)
        return LoginResult(dto.token, dto.user.toDomain())
    }

    override suspend fun login(email: String, password: String): LoginResult {
        val dto = source.login(email, password)
        return LoginResult(dto.token, dto.user.toDomain())
    }

    override suspend fun logout() = source.logout()

    override suspend fun verifyEmail(code: String): User = source.verifyEmail(code).toDomain()

    override suspend fun resendVerification(email: String): String = source.resendVerification(email)
}
