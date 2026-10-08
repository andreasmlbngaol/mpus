package id.andreasmlbngaol.mpus.sighting.data.mapper

import id.andreasmlbngaol.mpus.core.data.mapper.toDomain
import id.andreasmlbngaol.mpus.sighting.data.dto.CandidateDto
import id.andreasmlbngaol.mpus.sighting.data.dto.SightingResultDto
import id.andreasmlbngaol.mpus.sighting.domain.model.Candidate
import id.andreasmlbngaol.mpus.sighting.domain.model.SightingResult

fun CandidateDto.toDomain(): Candidate = Candidate(
    catId = catId,
    thumbUrl = thumbUrl,
    displayName = displayName,
    similarity = similarity,
)

fun SightingResultDto.toDomain(): SightingResult = SightingResult(
    sighting = sighting.toDomain(),
    candidates = candidates.map { it.toDomain() },
)
