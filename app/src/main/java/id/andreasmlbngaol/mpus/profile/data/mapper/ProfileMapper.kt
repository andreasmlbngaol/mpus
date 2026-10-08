package id.andreasmlbngaol.mpus.profile.data.mapper

import id.andreasmlbngaol.mpus.core.data.mapper.toDomain
import id.andreasmlbngaol.mpus.profile.data.dto.MySightingsDto
import id.andreasmlbngaol.mpus.profile.data.dto.UserProfileDto
import id.andreasmlbngaol.mpus.profile.domain.model.MySightings
import id.andreasmlbngaol.mpus.profile.domain.model.UserProfile

fun MySightingsDto.toDomain(): MySightings = MySightings(
    items = items.map { it.toDomain() },
    nextCursor = nextCursor,
    sightingsCount = sightingsCount,
    catsCount = catsCount,
    unnamedCount = unnamedCount,
)

fun UserProfileDto.toDomain(): UserProfile = UserProfile(
    id = id,
    username = username,
    nickname = nickname,
    avatarUrl = avatarUrl,
    sightingsCount = sightingsCount,
    catsCount = catsCount,
    namesCount = namesCount,
    cats = cats.toDomain { it.toDomain() },
)
