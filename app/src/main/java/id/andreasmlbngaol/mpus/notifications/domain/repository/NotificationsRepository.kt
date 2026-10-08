package id.andreasmlbngaol.mpus.notifications.domain.repository

import id.andreasmlbngaol.mpus.core.domain.model.MergeRequest
import id.andreasmlbngaol.mpus.core.domain.model.Page
import id.andreasmlbngaol.mpus.notifications.domain.model.AppNotification

interface NotificationsRepository {
    suspend fun notifications(cursor: String? = null): Page<AppNotification>
    suspend fun unreadCount(): Long
    suspend fun markSeen()
    suspend fun pendingMerges(): List<MergeRequest>
    suspend fun approveMerge(id: String): MergeRequest
    suspend fun rejectMerge(id: String)
}
