package id.andreasmlbngaol.mpus.core.data.mapper

import id.andreasmlbngaol.mpus.core.data.dto.CatMarkerDto
import id.andreasmlbngaol.mpus.core.data.dto.MergeRequestDto
import id.andreasmlbngaol.mpus.core.data.dto.PageDto
import id.andreasmlbngaol.mpus.core.data.dto.SightingDto
import id.andreasmlbngaol.mpus.core.domain.model.CatMarker
import id.andreasmlbngaol.mpus.core.domain.model.MergeRequest
import id.andreasmlbngaol.mpus.core.domain.model.Page
import id.andreasmlbngaol.mpus.core.domain.model.Sighting

fun CatMarkerDto.toDomain(): CatMarker = CatMarker(
    id = id,
    displayName = displayName,
    thumbUrl = thumbUrl,
    lat = lat,
    lng = lng,
    sightingCount = sightingCount,
)

fun SightingDto.toDomain(): Sighting = Sighting(
    id = id,
    catId = catId,
    photoUrl = photoUrl,
    thumbUrl = thumbUrl,
    lat = lat,
    lng = lng,
    takenAt = takenAt,
    createdAt = createdAt,
)

fun MergeRequestDto.toDomain(): MergeRequest = MergeRequest(
    id = id,
    sourceCatId = sourceCatId,
    targetCatId = targetCatId,
    sourceName = sourceName,
    targetName = targetName,
    requestedBy = requestedBy,
    status = status,
)

fun <T, R> PageDto<T>.toDomain(map: (T) -> R): Page<R> =
    Page(items = items.map(map), nextCursor = nextCursor)
