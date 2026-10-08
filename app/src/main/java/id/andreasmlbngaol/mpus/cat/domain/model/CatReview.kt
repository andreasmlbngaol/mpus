package id.andreasmlbngaol.mpus.cat.domain.model

/** One review of a cat: a note plus a 0-10 rating, independently likeable. */
data class CatReview(
    val id: String,
    val userId: String,
    val nickname: String,
    val body: String,
    val rating: Int,
    val likes: Int,
    val likedByMe: Boolean,
    /** True when you wrote this review; the composer hides once you have one. */
    val mine: Boolean = false,
    val createdAt: String,
)
