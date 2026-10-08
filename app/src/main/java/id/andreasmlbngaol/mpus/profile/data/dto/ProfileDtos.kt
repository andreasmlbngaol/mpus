package id.andreasmlbngaol.mpus.profile.data.dto

import id.andreasmlbngaol.mpus.core.data.dto.CatMarkerDto
import id.andreasmlbngaol.mpus.core.data.dto.PageDto
import id.andreasmlbngaol.mpus.core.data.dto.SightingDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /me/sightings`: one page plus the exact totals for the profile stats. */
@Serializable
data class MySightingsDto(
    val items: List<SightingDto> = emptyList(),
    @SerialName("next_cursor") val nextCursor: String? = null,
    @SerialName("sightings_count") val sightingsCount: Long = 0,
    @SerialName("cats_count") val catsCount: Long = 0,
    @SerialName("unnamed_count") val unnamedCount: Long = 0,
)

/** Someone else's public profile: who they are and what they've contributed. */
@Serializable
data class UserProfileDto(
    val id: String,
    val username: String,
    val nickname: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("sightings_count") val sightingsCount: Long = 0,
    @SerialName("cats_count") val catsCount: Long = 0,
    @SerialName("names_count") val namesCount: Long = 0,
    val cats: PageDto<CatMarkerDto> = PageDto(),
)

@Serializable
data class UnreadCountDto(val count: Long = 0)

@Serializable
data class PatchMeBody(val username: String? = null, val nickname: String? = null)
