package id.andreasmlbngaol.mpus.cat.data.dto

import id.andreasmlbngaol.mpus.core.data.dto.SightingDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /me/sightings`: only the fields the cat feature reads (for merge targets). */
@Serializable
data class MySightingsDto(
    val items: List<SightingDto> = emptyList(),
    @SerialName("next_cursor") val nextCursor: String? = null,
)
