package id.andreasmlbngaol.mpus.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Wire/cache shape of a user. The domain's `User` stays annotation-free. */
@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val nickname: String,
    val email: String? = null,
    @SerialName("email_verified") val emailVerified: Boolean? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)
