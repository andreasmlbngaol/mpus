package id.andreasmlbngaol.mpus.notifications.domain.model

/** One inbox entry: who did it, what they did, and which cat it was about. */
data class AppNotification(
    val id: String,
    val kind: String,
    val actorNickname: String? = null,
    val actorAvatarUrl: String? = null,
    val catId: String? = null,
    val catName: String? = null,
    val seen: Boolean = false,
    val createdAt: String,
)
