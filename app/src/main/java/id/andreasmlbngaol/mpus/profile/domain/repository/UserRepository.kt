package id.andreasmlbngaol.mpus.profile.domain.repository

import id.andreasmlbngaol.mpus.profile.domain.model.UserProfile

/** Someone else's public profile (the `user/{id}` screen). */
interface UserRepository {
    suspend fun profile(id: String, cursor: String? = null): UserProfile
}
