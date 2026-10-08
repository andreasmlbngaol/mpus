package id.andreasmlbngaol.mpus.profile.data.repository

import id.andreasmlbngaol.mpus.core.data.mapper.toDomain
import id.andreasmlbngaol.mpus.core.domain.model.User
import id.andreasmlbngaol.mpus.profile.data.mapper.toDomain
import id.andreasmlbngaol.mpus.profile.data.source.ProfileRemoteSource
import id.andreasmlbngaol.mpus.profile.domain.model.MySightings
import id.andreasmlbngaol.mpus.profile.domain.model.UserProfile
import id.andreasmlbngaol.mpus.profile.domain.repository.ProfileRepository
import id.andreasmlbngaol.mpus.profile.domain.repository.UserRepository

class ProfileRepositoryImpl(private val source: ProfileRemoteSource) : ProfileRepository {

    override suspend fun mySightings(cursor: String?): MySightings = source.mySightings(cursor).toDomain()

    override suspend fun unreadCount(): Long = source.unreadCount().count

    override suspend fun uploadAvatar(bytes: ByteArray, filename: String): User =
        source.uploadAvatar(bytes, filename).toDomain()

    override suspend fun deleteAvatar(): User = source.deleteAvatar().toDomain()

    override suspend fun updateProfile(username: String?, nickname: String?): User =
        source.updateProfile(username, nickname).toDomain()

    override suspend fun logout() = source.logout()
}

class UserRepositoryImpl(private val source: ProfileRemoteSource) : UserRepository {
    override suspend fun profile(id: String, cursor: String?): UserProfile =
        source.userProfile(id, cursor).toDomain()
}
