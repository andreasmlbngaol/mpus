package id.andreasmlbngaol.mpus.core.domain.model

/** A cat as it appears on the map and in profile grids. */
data class CatMarker(
    val id: String,
    val displayName: String? = null,
    val thumbUrl: String,
    val lat: Double,
    val lng: Double,
    val sightingCount: Long,
)
