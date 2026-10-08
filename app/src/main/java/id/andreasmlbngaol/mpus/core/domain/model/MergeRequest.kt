package id.andreasmlbngaol.mpus.core.domain.model

/** A pending or resolved cat merge. Shared by the cat detail and the notifications inbox. */
data class MergeRequest(
    val id: String,
    val sourceCatId: String,
    val targetCatId: String,
    val sourceName: String? = null,
    val targetName: String? = null,
    val requestedBy: String,
    val status: String,
)
