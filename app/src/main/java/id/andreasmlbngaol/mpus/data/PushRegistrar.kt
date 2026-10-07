package id.andreasmlbngaol.mpus.data

import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Keeps this device's FCM token registered with the backend, tied to the signed-in
 * session. Called when a session appears (sign-in, or a cold start already signed in)
 * and when one goes away (sign-out).
 *
 * `open` so unit tests can subclass it: the real one talks to Firebase, which isn't
 * available off-device.
 */
open class PushRegistrar(
    private val api: ApiClient,
    private val session: Session,
) {
    /** Fetch the current FCM token and claim it for the signed-in user. */
    open suspend fun registerCurrent() {
        val token = runCatching { currentToken() }.getOrNull() ?: return
        runCatching { api.registerDevice(token) }
    }

    /** A rotated token arrives from `onNewToken`; only claim it if someone is signed in. */
    open suspend fun onTokenRefreshed(token: String) {
        if (session.token.value != null) runCatching { api.registerDevice(token) }
    }

    /** Forget the token on sign-out so a signed-out phone stops getting this user's pushes. */
    open suspend fun unregisterCurrent() {
        val token = runCatching { currentToken() }.getOrNull() ?: return
        runCatching { api.unregisterDevice(token) }
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
