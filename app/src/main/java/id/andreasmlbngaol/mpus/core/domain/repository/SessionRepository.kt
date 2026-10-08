package id.andreasmlbngaol.mpus.core.domain.repository

import id.andreasmlbngaol.mpus.core.domain.model.User
import kotlinx.coroutines.flow.StateFlow

/**
 * The session surface the rest of the app depends on: the opaque auth token and the
 * cached user, both observable. The real one is SharedPreferences-backed; tests use an
 * in-memory fake.
 */
interface SessionRepository {
    val token: StateFlow<String?>
    val user: StateFlow<User?>
    fun save(token: String, user: User)
    fun updateUser(user: User)
    fun clear()
}
