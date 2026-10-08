package id.andreasmlbngaol.mpus.cat.data.mapper

import id.andreasmlbngaol.mpus.cat.data.dto.CatDetailDto
import id.andreasmlbngaol.mpus.cat.data.dto.CatNameDto
import id.andreasmlbngaol.mpus.cat.data.dto.CatReviewDto
import id.andreasmlbngaol.mpus.cat.domain.model.CatDetail
import id.andreasmlbngaol.mpus.cat.domain.model.CatName
import id.andreasmlbngaol.mpus.cat.domain.model.CatReview
import id.andreasmlbngaol.mpus.core.data.mapper.toDomain

fun CatNameDto.toDomain(): CatName = CatName(
    id = id,
    userId = userId,
    nickname = nickname,
    name = name,
    likes = likes,
    likedByMe = likedByMe,
    mine = mine,
)

fun CatReviewDto.toDomain(): CatReview = CatReview(
    id = id,
    userId = userId,
    nickname = nickname,
    body = body,
    rating = rating,
    likes = likes,
    likedByMe = likedByMe,
    mine = mine,
    createdAt = createdAt,
)

fun CatDetailDto.toDomain(): CatDetail = CatDetail(
    id = id,
    displayName = displayName,
    createdAt = createdAt,
    names = names.map { it.toDomain() },
    reviews = reviews.map { it.toDomain() },
    sightings = sightings.map { it.toDomain() },
    canContribute = canContribute,
)
