package id.andreasmlbngaol.mpus.notifications.ui

import androidx.compose.runtime.Immutable
import id.andreasmlbngaol.mpus.core.domain.model.MergeRequest
import id.andreasmlbngaol.mpus.core.ui.UiText
import id.andreasmlbngaol.mpus.notifications.domain.model.AppNotification

@Immutable
data class NotificationsUiState(
    val items: List<AppNotification> = emptyList(),
    val nextCursor: String? = null,
    val loadingMore: Boolean = false,
    val merges: List<MergeRequest> = emptyList(),
    val loading: Boolean = true,
    val error: UiText? = null,
)
