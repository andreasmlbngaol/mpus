package id.andreasmlbngaol.mpus.sighting.domain.model

/** A possible existing cat the new photo might match, with the similarity score. */
data class Candidate(
    val catId: String,
    val thumbUrl: String,
    val displayName: String? = null,
    val similarity: Double,
)
