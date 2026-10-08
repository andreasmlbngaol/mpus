package id.andreasmlbngaol.mpus.map.domain.model

/** A map viewport box, in degrees. */
data class Bounds(
    val minLat: Double,
    val minLng: Double,
    val maxLat: Double,
    val maxLng: Double,
)
