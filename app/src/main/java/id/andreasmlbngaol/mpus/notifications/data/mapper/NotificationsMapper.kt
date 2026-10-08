package id.andreasmlbngaol.mpus.notifications.data.mapper

import id.andreasmlbngaol.mpus.notifications.data.dto.AppNotificationDto
import id.andreasmlbngaol.mpus.notifications.domain.model.AppNotification

fun AppNotificationDto.toDomain(): AppNotification = AppNotification(
    id = id,
    kind = kind,
    actorNickname = actorNickname,
    actorAvatarUrl = actorAvatarUrl,
    catId = catId,
    catName = catName,
    seen = seen,
    createdAt = createdAt,
)
