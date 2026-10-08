package id.andreasmlbngaol.mpus.profile.domain.usecase

import id.andreasmlbngaol.mpus.profile.domain.model.UserProfile
import id.andreasmlbngaol.mpus.profile.domain.repository.UserRepository

/** Someone else's profile page. */
class UserUseCase(private val repo: UserRepository) {
    suspend fun profile(id: String, cursor: String? = null): UserProfile = repo.profile(id, cursor)
}
