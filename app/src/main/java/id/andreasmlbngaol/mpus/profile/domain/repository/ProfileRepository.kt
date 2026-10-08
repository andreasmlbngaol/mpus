package id.andreasmlbngaol.mpus.profile.domain.repository

import id.andreasmlbngaol.mpus.core.domain.model.User
import id.andreasmlbngaol.mpus.profile.domain.model.MySightings

/** The signed-in user's own profile: their sightings, avatar, edits, and sign-out. */
interface ProfileRepository {
    suspend fun mySightings(cursor: String? = null): MySightings

    /** The bell badge count. */
    suspend fun unreadCount(): Long

    suspend fun uploadAvatar(bytes: ByteArray, filename: String): User
    suspend fun deleteAvatar(): User

    /** Update the editable fields; returns the updated user. */
    suspend fun updateProfile(username: String?, nickname: String?): User

    suspend fun logout()
}
