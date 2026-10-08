package id.andreasmlbngaol.mpus.sighting.domain.model

import id.andreasmlbngaol.mpus.core.domain.model.Sighting

/** The result of uploading a sighting: the stored photo plus any look-alike cats. */
data class SightingResult(
    val sighting: Sighting,
    val candidates: List<Candidate> = emptyList(),
)
