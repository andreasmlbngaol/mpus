package id.andreasmlbngaol.mpus.profile.ui

/** Everything the user can do on someone else's profile page. */
sealed interface UserUiEvent {
    data object Refresh : UserUiEvent
    data object LoadMore : UserUiEvent
}
