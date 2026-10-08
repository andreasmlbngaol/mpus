package id.andreasmlbngaol.mpus.core.domain.model

/** A notification tap that wants to open a screen. [seq] makes identical taps distinct. */
data class DeepLinkTarget(
    val kind: String?,
    val catId: String?,
    val seq: Long,
)
