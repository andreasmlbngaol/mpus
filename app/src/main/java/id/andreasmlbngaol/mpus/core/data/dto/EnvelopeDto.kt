package id.andreasmlbngaol.mpus.core.data.dto

import kotlinx.serialization.Serializable

/** The backend's success/failure envelope. `data` is absent on message-only replies. */
@Serializable
data class EnvelopeDto<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
)

/** Same envelope without `data`, for message-only endpoints. */
@Serializable
data class MessageEnvelopeDto(
    val success: Boolean,
    val message: String? = null,
)
