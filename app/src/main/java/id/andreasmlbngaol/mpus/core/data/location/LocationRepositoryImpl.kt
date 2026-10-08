package id.andreasmlbngaol.mpus.core.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import id.andreasmlbngaol.mpus.core.domain.model.GeoPoint
import id.andreasmlbngaol.mpus.core.domain.repository.LocationRepository

class LocationRepositoryImpl(private val context: Context) : LocationRepository {

    override fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Best-effort one-shot fix: prefers a fresh platform location, falls back to the most
     * recent known fix from any enabled provider.
     *
     * ponytail: falls back to last-known; swap to a LocationRequest loop for continuous tracking.
     */
    override suspend fun current(): GeoPoint? {
        if (!hasPermission()) return null
        val lm = context.getSystemService(LocationManager::class.java) ?: return null
        val providers = runCatching { lm.getProviders(true) }.getOrDefault(emptyList())

        val last = providers
            .mapNotNull { p -> runCatching { lm.getLastKnownLocation(p) }.getOrNull() }
            .maxByOrNull { it.time }

        val fresh = last?.takeIf { System.currentTimeMillis() - it.time < 5 * 60_000 }
        return (fresh ?: last)?.let { GeoPoint(it.latitude, it.longitude) }
    }
}
