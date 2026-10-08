package id.andreasmlbngaol.mpus.notifications.domain.usecase

import id.andreasmlbngaol.mpus.core.domain.model.MergeRequest
import id.andreasmlbngaol.mpus.core.domain.model.Page
import id.andreasmlbngaol.mpus.notifications.domain.model.AppNotification
import id.andreasmlbngaol.mpus.notifications.domain.repository.NotificationsRepository

class NotificationsUseCase(private val repo: NotificationsRepository) {
    suspend fun notifications(cursor: String? = null): Page<AppNotification> = repo.notifications(cursor)
    suspend fun markSeen() = repo.markSeen()
    suspend fun pendingMerges(): List<MergeRequest> = repo.pendingMerges()
    suspend fun approveMerge(id: String): MergeRequest = repo.approveMerge(id)
    suspend fun rejectMerge(id: String) = repo.rejectMerge(id)
}
