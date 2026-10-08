package id.andreasmlbngaol.mpus.core.domain.repository

/**
 * Keeps this device's push token registered with the backend, tied to the session. The
 * real implementation talks to Firebase; nothing above it needs to know that.
 */
interface PushRepository {
    suspend fun registerCurrent()
    suspend fun onTokenRefreshed(token: String)
    suspend fun unregisterCurrent()
}
