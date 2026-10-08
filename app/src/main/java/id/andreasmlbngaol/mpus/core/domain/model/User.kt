package id.andreasmlbngaol.mpus.core.domain.model

/** The signed-in user. Shared by every feature that shows or edits an identity. */
data class User(
    val id: String,
    val username: String,
    val nickname: String,
    val email: String? = null,
    val emailVerified: Boolean? = null,
    val avatarUrl: String? = null,
)
