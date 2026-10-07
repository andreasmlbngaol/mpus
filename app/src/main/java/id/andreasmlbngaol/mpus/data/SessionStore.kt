package id.andreasmlbngaol.mpus.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * The session surface the rest of the app depends on. [SessionStore] is the real,
 * SharedPreferences-backed one; tests use a trivial in-memory fake. Keeping the
 * ViewModels on this interface is what lets them be unit-tested off-device.
 */
interface Session {
    val token: StateFlow<String?>
    val user: StateFlow<User?>
    fun save(token: String, user: User)
    fun updateUser(user: User)
    fun clear()
}

/**
 * Holds the session token and cached user, persisted in SharedPreferences.
 * The token is sent as `Authorization: Bearer <token>` on every authed call.
 */
class SessionStore(context: Context) : Session {
    private val prefs = context.getSharedPreferences("mpus_session", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _token = MutableStateFlow(prefs.getString(KEY_TOKEN, null))
    override val token: StateFlow<String?> = _token.asStateFlow()

    private val _user = MutableStateFlow(
        prefs.getString(KEY_USER, null)?.let { runCatching { json.decodeFromString<User>(it) }.getOrNull() },
    )
    override val user: StateFlow<User?> = _user.asStateFlow()

    override fun save(token: String, user: User) {
        prefs.edit().putString(KEY_TOKEN, token).putString(KEY_USER, json.encodeToString(user)).apply()
        _token.value = token
        _user.value = user
    }

    override fun updateUser(user: User) {
        prefs.edit().putString(KEY_USER, json.encodeToString(user)).apply()
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
