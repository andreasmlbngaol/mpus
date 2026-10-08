package id.andreasmlbngaol.mpus.auth.domain.model

import id.andreasmlbngaol.mpus.core.domain.model.User

/** A fresh session: the opaque token plus the user it belongs to. */
data class LoginResult(val token: String, val user: User)
