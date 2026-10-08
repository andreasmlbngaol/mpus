package id.andreasmlbngaol.mpus.cat.domain.model

/** One suggested name for a cat, with its like tally and whether the viewer liked it. */
data class CatName(
    val id: String,
    val userId: String,
    val nickname: String,
    val name: String,
    val likes: Int,
    val likedByMe: Boolean,
    /** True when you wrote this name; the composer hides once you have one. */
    val mine: Boolean = false,
)
