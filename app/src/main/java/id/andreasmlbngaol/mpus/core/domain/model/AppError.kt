package id.andreasmlbngaol.mpus.core.domain.model

/**
 * Every failure the app can surface, translated from whatever the data layer threw.
 *
 * This is the domain's own vocabulary: no Ktor, no OkHttp, no JSON type leaks here. The
 * data layer maps transport errors onto these; the UI maps these onto [UiText].
 */
sealed class AppError(message: String) : Exception(message) {
    /** Couldn't reach the server at all: DNS, refused connection, timeout. */
    class Network(message: String = "Network unreachable") : AppError(message)

    /** The session is missing or no longer valid (401), or a write was refused (403). */
    class Unauthorized(message: String) : AppError(message)

    /** The server answered but rejected the call: envelope `success=false`, or a raw 4xx/5xx. */
    class Server(val code: Int, message: String) : AppError(message)

    /** Anything we couldn't classify. */
    class Unknown(message: String) : AppError(message)
}
