package id.andreasmlbngaol.mpus.cat.ui

import id.andreasmlbngaol.mpus.core.ui.UiText

/** Everything the user can do on the cat detail page. */
sealed interface CatUiEvent {
    data object Refresh : CatUiEvent
    data class NameSubmitted(val name: String) : CatUiEvent
    data class NameLiked(val nameId: String) : CatUiEvent
    data class ReviewSubmitted(val body: String, val rating: Int) : CatUiEvent
    data class ReviewLiked(val reviewId: String) : CatUiEvent
    data class Report(val kind: String, val targetId: String) : CatUiEvent
    data object LoadMergeTargets : CatUiEvent
    data class MergeInto(val targetId: String) : CatUiEvent
}

/** One-shot outputs the screen reacts to once. */
sealed interface CatUiEffect {
    data class ShowMessage(val text: UiText) : CatUiEffect
}
