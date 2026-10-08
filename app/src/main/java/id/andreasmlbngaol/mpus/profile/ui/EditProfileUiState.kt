package id.andreasmlbngaol.mpus.profile.ui

import androidx.compose.runtime.Immutable

@Immutable
data class EditProfileUiState(
    val username: String = "",
    val nickname: String = "",
    val busy: Boolean = false,
    val saved: Boolean = false,
)
