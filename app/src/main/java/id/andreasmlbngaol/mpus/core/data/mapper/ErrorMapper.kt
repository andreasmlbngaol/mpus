package id.andreasmlbngaol.mpus.core.data.mapper

import id.andreasmlbngaol.mpus.core.domain.model.AppError
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

/**
 * Turns anything the transport throws into the domain's [AppError]. This is the single
 * place that knows about Ktor/serialization exceptions — nothing above data ever sees one.
 */
fun Throwable.toAppError(): AppError = when (this) {
    is AppError -> this
    // Never swallow coroutine cancellation; let it propagate so scopes cancel cleanly.
    is CancellationException -> throw this
    is SerializationException -> AppError.Unknown(message ?: "Malformed response")
    is java.io.IOException -> AppError.Network(message ?: "Network unreachable")
    else -> AppError.Unknown(message ?: this::class.simpleName ?: "Unknown error")
}
