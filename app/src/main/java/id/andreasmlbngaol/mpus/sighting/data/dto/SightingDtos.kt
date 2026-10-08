package id.andreasmlbngaol.mpus.sighting.data.dto

import id.andreasmlbngaol.mpus.core.data.dto.SightingDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CandidateDto(
    @SerialName("cat_id") val catId: String,
    @SerialName("thumb_url") val thumbUrl: String,
    @SerialName("display_name") val displayName: String? = null,
    val similarity: Double,
)

@Serializable
data class SightingResultDto(
    val sighting: SightingDto,
    val candidates: List<CandidateDto> = emptyList(),
)

@Serializable
data class ResolveDataDto(@SerialName("cat_id") val catId: String)
