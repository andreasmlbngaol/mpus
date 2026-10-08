package id.andreasmlbngaol.mpus.core.data.network

import id.andreasmlbngaol.mpus.core.data.dto.EnvelopeDto
import id.andreasmlbngaol.mpus.core.data.dto.MessageEnvelopeDto
import id.andreasmlbngaol.mpus.core.data.mapper.toAppError
import id.andreasmlbngaol.mpus.core.domain.model.AppError
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

/**
 * Runs a Ktor call and unwraps the backend envelope into either the payload or an
 * [AppError]. Non-2xx and `success=false` both become [AppError.Server]; a non-JSON body
 * (axum's plain-text extractor errors, or `/health`) falls back to the raw text.
 */
suspend inline fun <reified T> apiCall(
    json: Json,
    crossinline block: suspend () -> HttpResponse,
): T {
    val response = try {
        block()
    } catch (e: Exception) {
        throw e.toAppError()
    }
    val text = response.bodyAsText()
    if (!response.status.isSuccess()) {
        throw AppError.Server(response.status.value, messageFrom(json, text))
    }
    val env = runCatching { json.decodeFromString<EnvelopeDto<T>>(text) }.getOrNull()
        ?: throw AppError.Unknown("Malformed response")
    if (!env.success) throw AppError.Server(response.status.value, env.message ?: "Request failed.")
    return env.data ?: throw AppError.Server(response.status.value, env.message ?: "No data returned.")
}

/** For message-only endpoints (logout, devices, resend, reject…): no `data` key. */
suspend inline fun apiCallMessage(
    json: Json,
    crossinline block: suspend () -> HttpResponse,
): String {
    val response = try {
        block()
    } catch (e: Exception) {
        throw e.toAppError()
    }
    val text = response.bodyAsText()
    if (!response.status.isSuccess()) {
        throw AppError.Server(response.status.value, messageFrom(json, text))
    }
    val env = runCatching { json.decodeFromString<MessageEnvelopeDto>(text) }.getOrNull()
    if (env != null && !env.success) throw AppError.Server(response.status.value, env.message ?: "Request failed.")
    return env?.message.orEmpty()
}

@PublishedApi
internal fun messageFrom(json: Json, text: String): String =
    runCatching { json.decodeFromString<MessageEnvelopeDto>(text).message }.getOrNull()
        ?: text.takeIf { it.isNotBlank() }?.take(300)
        ?: "Something went wrong."
