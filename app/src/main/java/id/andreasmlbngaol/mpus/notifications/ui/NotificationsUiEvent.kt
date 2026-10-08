package id.andreasmlbngaol.mpus.notifications.ui

import id.andreasmlbngaol.mpus.core.ui.UiText

/** Everything the user can do on the notifications inbox. */
sealed interface NotificationsUiEvent {
    data object Refresh : NotificationsUiEvent
    data object LoadMore : NotificationsUiEvent
    data class Approve(val id: String) : NotificationsUiEvent
    data class Reject(val id: String) : NotificationsUiEvent
}

sealed interface NotificationsUiEffect {
    data class ShowMessage(val text: UiText) : NotificationsUiEffect
}
