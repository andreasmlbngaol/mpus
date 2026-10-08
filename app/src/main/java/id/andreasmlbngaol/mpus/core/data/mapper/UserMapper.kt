package id.andreasmlbngaol.mpus.core.data.mapper

import id.andreasmlbngaol.mpus.core.data.dto.UserDto
import id.andreasmlbngaol.mpus.core.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id,
    username = username,
    nickname = nickname,
    email = email,
    emailVerified = emailVerified,
    avatarUrl = avatarUrl,
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    username = username,
    nickname = nickname,
    email = email,
    emailVerified = emailVerified,
    avatarUrl = avatarUrl,
)
