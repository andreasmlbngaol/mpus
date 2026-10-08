package id.andreasmlbngaol.mpus.core.data.push

import com.google.firebase.messaging.FirebaseMessaging
import id.andreasmlbngaol.mpus.core.domain.repository.DeviceRepository
import id.andreasmlbngaol.mpus.core.domain.repository.PushRepository
import id.andreasmlbngaol.mpus.core.domain.repository.SessionRepository
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Keeps this device's FCM token registered with the backend, tied to the signed-in
 * session: called when a session appears (sign-in, cold start) and goes away (sign-out).
 *
 * `open` so unit tests can subclass it: the real one talks to Firebase, unavailable off-device.
 */
open class PushRegistrar(
    private val devices: DeviceRepository,
    private val session: SessionRepository,
) : PushRepository {
    /** Fetch the current FCM token and claim it for the signed-in user. */
    override suspend fun registerCurrent() {
        val token = runCatching { currentToken() }.getOrNull() ?: return
        runCatching { devices.register(token) }
    }

    /** A rotated token arrives from `onNewToken`; only claim it if someone is signed in. */
    override suspend fun onTokenRefreshed(token: String) {
        if (session.token.value != null) runCatching { devices.register(token) }
    }

    /** Forget the token on sign-out so a signed-out phone stops getting this user's pushes. */
    override suspend fun unregisterCurrent() {
        val token = runCatching { currentToken() }.getOrNull() ?: return
        runCatching { devices.unregister(token) }
    }

    // `getToken` is flagged deprecated with no public replacement in firebase-messaging
    // 25.x (the alternate path is package-private). It is still the only way to get the
    // FCM registration token, so we take the warning.
    @Suppress("DEPRECATION")
    private suspend fun currentToken(): String? = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            cont.resume(if (task.isSuccessful) task.result else null)
        }
    }
}
