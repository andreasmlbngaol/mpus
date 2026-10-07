package id.andreasmlbngaol.mpus.data

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class User(
    val id: String,
    val username: String,
    val nickname: String,
    val email: String? = null,
    @SerialName("email_verified") val emailVerified: Boolean? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

@Immutable
@Serializable
data class LoginData(val token: String, val user: User)

@Immutable
@Serializable
data class Sighting(
    val id: String,
    @SerialName("cat_id") val catId: String? = null,
    @SerialName("photo_url") val photoUrl: String,
    @SerialName("thumb_url") val thumbUrl: String,
    val lat: Double,
    val lng: Double,
    @SerialName("taken_at") val takenAt: String? = null,
    @SerialName("created_at") val createdAt: String,
)

@Immutable
@Serializable
data class Candidate(
    @SerialName("cat_id") val catId: String,
    @SerialName("thumb_url") val thumbUrl: String,
    @SerialName("display_name") val displayName: String? = null,
    val similarity: Double,
)

@Immutable
@Serializable
data class SightingResult(
    val sighting: Sighting,
    val candidates: List<Candidate> = emptyList(),
)

@Immutable
@Serializable
data class ResolveData(@SerialName("cat_id") val catId: String)

@Immutable
@Serializable
data class CatMarker(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("thumb_url") val thumbUrl: String,
    val lat: Double,
    val lng: Double,
    @SerialName("sighting_count") val sightingCount: Long,
)

@Immutable
@Serializable
data class CatName(
    val id: String,
    @SerialName("user_id") val userId: String,
    val nickname: String,
    val name: String,
    val likes: Int,
    @SerialName("liked_by_me") val likedByMe: Boolean,
)

@Immutable
@Serializable
data class CatReview(
    val id: String,
    @SerialName("user_id") val userId: String,
    val nickname: String,
    val body: String,
    val rating: Int,
    val likes: Int,
    @SerialName("liked_by_me") val likedByMe: Boolean,
    @SerialName("created_at") val createdAt: String,
)

@Immutable
@Serializable
data class CatDetail(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("created_at") val createdAt: String,
    val names: List<CatName> = emptyList(),
    val reviews: List<CatReview> = emptyList(),
    val sightings: List<Sighting> = emptyList(),
    @SerialName("can_contribute") val canContribute: Boolean = false,
)

@Immutable
@Serializable
data class LikeData(val liked: Boolean)

/** A window into a longer list. `nextCursor` is null on the last page. */
@Immutable
@Serializable
data class Page<T>(
    val items: List<T> = emptyList(),
    @SerialName("next_cursor") val nextCursor: String? = null,
)

/** `GET /me/sightings`: one page plus the exact totals for the profile stats. */
@Immutable
@Serializable
data class MySightings(
    val items: List<Sighting> = emptyList(),
    @SerialName("next_cursor") val nextCursor: String? = null,
    @SerialName("sightings_count") val sightingsCount: Long = 0,
    @SerialName("cats_count") val catsCount: Long = 0,
    @SerialName("unnamed_count") val unnamedCount: Long = 0,
)

/** Someone else's public profile: who they are and what they've contributed. */
@Immutable
@Serializable
data class UserProfile(
    val id: String,
    val username: String,
    val nickname: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("sightings_count") val sightingsCount: Long = 0,
    @SerialName("cats_count") val catsCount: Long = 0,
    @SerialName("names_count") val namesCount: Long = 0,
    val cats: Page<CatMarker> = Page(),
)

/** One inbox entry: who did it, what they did, and which cat it was about. */
@Immutable
@Serializable
data class AppNotification(
    val id: String,
    val kind: String,
    @SerialName("actor_nickname") val actorNickname: String? = null,
    @SerialName("actor_avatar_url") val actorAvatarUrl: String? = null,
    @SerialName("cat_id") val catId: String? = null,
    @SerialName("cat_name") val catName: String? = null,
    val seen: Boolean = false,
    @SerialName("created_at") val createdAt: String,
)

/** The bell badge count. */
@Immutable
@Serializable
data class UnreadCount(val count: Long = 0)

/** A pending or resolved cat merge. */
@Immutable
@Serializable
data class MergeRequest(
    val id: String,
    @SerialName("source_cat_id") val sourceCatId: String,
    @SerialName("target_cat_id") val targetCatId: String,
    @SerialName("source_name") val sourceName: String? = null,
    @SerialName("target_name") val targetName: String? = null,
    @SerialName("requested_by") val requestedBy: String,
    val status: String,
)

/** Whether a report just pushed an item over the hide threshold. */
@Immutable
@Serializable
data class ReportResult(val hidden: Boolean = false)

// ---- Envelopes -------------------------------------------------------------

@Serializable
internal data class Envelope<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
)

@Serializable
internal data class MessageEnvelope(
    val success: Boolean,
    val message: String? = null,
)
