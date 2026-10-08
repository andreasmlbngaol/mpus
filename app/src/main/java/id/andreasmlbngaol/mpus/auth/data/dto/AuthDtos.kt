package id.andreasmlbngaol.mpus.auth.data.dto

import id.andreasmlbngaol.mpus.core.data.dto.UserDto
import kotlinx.serialization.Serializable

/** `POST /auth/login` and `/auth/signup`: a token plus the user it belongs to. */
@Serializable
data class LoginDataDto(val token: String, val user: UserDto)

@Serializable
data class SignupBody(
    val email: String,
    val password: String,
    val username: String,
    val nickname: String,
)

@Serializable
data class LoginBody(val email: String, val password: String)

@Serializable
data class VerifyEmailBody(val token: String)

@Serializable
data class EmailBody(val email: String)
