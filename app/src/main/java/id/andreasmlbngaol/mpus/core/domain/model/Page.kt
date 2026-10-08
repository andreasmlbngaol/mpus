package id.andreasmlbngaol.mpus.core.domain.model

/** A window into a longer list. [nextCursor] is null on the last page. */
data class Page<T>(
    val items: List<T> = emptyList(),
    val nextCursor: String? = null,
)
