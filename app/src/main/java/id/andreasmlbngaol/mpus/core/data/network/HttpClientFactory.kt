package id.andreasmlbngaol.mpus.core.data.network

import id.andreasmlbngaol.mpus.core.domain.repository.SessionRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** The one JSON config the whole app shares (lenient about unknown/absent fields). */
fun createJson(): Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

/**
 * The one Ktor client the whole app shares. Feature remote sources just make requests on
 * it; base URL and the bearer token are attached here so no source repeats them.
 */
fun createHttpClient(baseUrl: String, session: SessionRepository, json: Json): HttpClient =
    HttpClient(CIO) {
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            connectTimeoutMillis = 20_000
            requestTimeoutMillis = 90_000
            socketTimeoutMillis = 90_000
        }
        // We read the envelope ourselves; non-2xx is not thrown as an exception.
        expectSuccess = false
        defaultRequest {
            url(baseUrl)
            session.token.value?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }
    }
