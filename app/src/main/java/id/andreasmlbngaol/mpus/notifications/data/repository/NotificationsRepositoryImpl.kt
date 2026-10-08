package id.andreasmlbngaol.mpus.notifications.data.repository

import id.andreasmlbngaol.mpus.core.data.mapper.toDomain
import id.andreasmlbngaol.mpus.core.domain.model.MergeRequest
import id.andreasmlbngaol.mpus.core.domain.model.Page
import id.andreasmlbngaol.mpus.notifications.data.mapper.toDomain
import id.andreasmlbngaol.mpus.notifications.data.source.NotificationsRemoteSource
import id.andreasmlbngaol.mpus.notifications.domain.model.AppNotification
import id.andreasmlbngaol.mpus.notifications.domain.repository.NotificationsRepository

class NotificationsRepositoryImpl(private val source: NotificationsRemoteSource) : NotificationsRepository {

    override suspend fun notifications(cursor: String?): Page<AppNotification> =
        source.notifications(cursor).toDomain { it.toDomain() }

    override suspend fun unreadCount(): Long = source.unreadCount()

    override suspend fun markSeen() {
        source.markSeen()
    }

    override suspend fun pendingMerges(): List<MergeRequest> = source.pendingMerges().map { it.toDomain() }

    override suspend fun approveMerge(id: String): MergeRequest = source.approveMerge(id).toDomain()

    override suspend fun rejectMerge(id: String) {
        source.rejectMerge(id)
    }
}
