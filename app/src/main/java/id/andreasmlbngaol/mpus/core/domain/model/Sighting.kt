package id.andreasmlbngaol.mpus.core.domain.model

/** One photo of a cat, wherever it's shown (cat detail, profile grid, sighting result). */
data class Sighting(
    val id: String,
    val catId: String? = null,
    val photoUrl: String,
    val thumbUrl: String,
    val lat: Double,
    val lng: Double,
    val takenAt: String? = null,
    val createdAt: String,
)
