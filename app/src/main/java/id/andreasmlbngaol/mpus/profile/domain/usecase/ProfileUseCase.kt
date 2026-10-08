package id.andreasmlbngaol.mpus.profile.domain.usecase

import id.andreasmlbngaol.mpus.core.domain.model.User
import id.andreasmlbngaol.mpus.profile.domain.model.MySightings
import id.andreasmlbngaol.mpus.profile.domain.repository.ProfileRepository

/** Everything the "You" tab does. */
class ProfileUseCase(private val repo: ProfileRepository) {
    suspend fun mySightings(cursor: String? = null): MySightings = repo.mySightings(cursor)
    suspend fun unreadCount(): Long = repo.unreadCount()
    suspend fun uploadAvatar(bytes: ByteArray, filename: String): User = repo.uploadAvatar(bytes, filename)
    suspend fun deleteAvatar(): User = repo.deleteAvatar()
    suspend fun updateProfile(username: String?, nickname: String?): User = repo.updateProfile(username, nickname)
    suspend fun logout() = repo.logout()
}
