package id.andreasmlbngaol.mpus.core.data.session

import android.content.Context
import id.andreasmlbngaol.mpus.core.data.dto.UserDto
import id.andreasmlbngaol.mpus.core.data.mapper.toDomain
import id.andreasmlbngaol.mpus.core.data.mapper.toDto
import id.andreasmlbngaol.mpus.core.domain.model.User
import id.andreasmlbngaol.mpus.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * Holds the session token and cached user in SharedPreferences. The token rides along as
 * `Authorization: Bearer <token>` on every authed call (attached in the HTTP client).
 */
class SessionRepositoryImpl(context: Context, private val json: Json) : SessionRepository {
    private val prefs = context.getSharedPreferences("mpus_session", Context.MODE_PRIVATE)

    private val _token = MutableStateFlow(prefs.getString(KEY_TOKEN, null))
    override val token: StateFlow<String?> = _token.asStateFlow()

    private val _user = MutableStateFlow(
        prefs.getString(KEY_USER, null)
            ?.let { runCatching { json.decodeFromString<UserDto>(it).toDomain() }.getOrNull() },
    )
    override val user: StateFlow<User?> = _user.asStateFlow()

    override fun save(token: String, user: User) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER, json.encodeToString(user.toDto()))
            .apply()
        _token.value = token
        _user.value = user
    }

    override fun updateUser(user: User) {
        prefs.edit().putString(KEY_USER, json.encodeToString(user.toDto())).apply()
        _user.value = user
    }

    override fun clear() {
        prefs.edit().clear().apply()
        _token.value = null
        _user.value = null
    }

    private companion object {
        const val KEY_TOKEN = "token"
        const val KEY_USER = "user"
    }
}
