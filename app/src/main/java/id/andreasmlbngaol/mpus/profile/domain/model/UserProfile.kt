package id.andreasmlbngaol.mpus.profile.domain.model

import id.andreasmlbngaol.mpus.core.domain.model.CatMarker
import id.andreasmlbngaol.mpus.core.domain.model.Page

/** Someone else's public profile: who they are and what they've contributed. */
data class UserProfile(
    val id: String,
    val username: String,
    val nickname: String,
    val avatarUrl: String? = null,
    val sightingsCount: Long = 0,
    val catsCount: Long = 0,
    val namesCount: Long = 0,
    val cats: Page<CatMarker> = Page(),
)
