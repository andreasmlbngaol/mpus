package id.andreasmlbngaol.mpus.cat.domain.model

import id.andreasmlbngaol.mpus.core.domain.model.Sighting

/** Everything the cat detail page shows: the cat, its names, reviews, and photos. */
data class CatDetail(
    val id: String,
    val displayName: String? = null,
    val createdAt: String,
    val names: List<CatName> = emptyList(),
    val reviews: List<CatReview> = emptyList(),
    val sightings: List<Sighting> = emptyList(),
    val canContribute: Boolean = false,
)
