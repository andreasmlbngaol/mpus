package id.andreasmlbngaol.mpus.core.domain.usecase

import id.andreasmlbngaol.mpus.core.domain.model.User
import id.andreasmlbngaol.mpus.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.StateFlow

/** The session as the UI sees it: who is signed in, and how to change that. */
class SessionUseCase(private val session: SessionRepository) {
    val token: StateFlow<String?> = session.token
    val user: StateFlow<User?> = session.user

    fun save(token: String, user: User) = session.save(token, user)
    fun updateUser(user: User) = session.updateUser(user)
    fun clear() = session.clear()
}
