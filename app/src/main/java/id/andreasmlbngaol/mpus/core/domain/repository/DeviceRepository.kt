package id.andreasmlbngaol.mpus.core.domain.repository

/** Registers/forgets this device's push token with the backend, tied to the session. */
interface DeviceRepository {
    suspend fun register(token: String, platform: String = "android")
    suspend fun unregister(token: String)
}
