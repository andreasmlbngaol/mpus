package id.andreasmlbngaol.mpus.profile.domain.model

import id.andreasmlbngaol.mpus.core.domain.model.Sighting

/** `GET /me/sightings`: one page plus the exact totals for the profile stats. */
data class MySightings(
    val items: List<Sighting> = emptyList(),
    val nextCursor: String? = null,
    val sightingsCount: Long = 0,
    val catsCount: Long = 0,
    val unnamedCount: Long = 0,
)
