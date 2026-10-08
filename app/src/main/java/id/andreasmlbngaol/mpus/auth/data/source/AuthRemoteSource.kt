package id.andreasmlbngaol.mpus.auth.data.source

import id.andreasmlbngaol.mpus.auth.data.dto.EmailBody
import id.andreasmlbngaol.mpus.auth.data.dto.LoginBody
import id.andreasmlbngaol.mpus.auth.data.dto.LoginDataDto
import id.andreasmlbngaol.mpus.auth.data.dto.SignupBody
import id.andreasmlbngaol.mpus.auth.data.dto.VerifyEmailBody
import id.andreasmlbngaol.mpus.core.data.network.apiCall
import id.andreasmlbngaol.mpus.core.data.network.apiCallMessage
import id.andreasmlbngaol.mpus.core.data.dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class AuthRemoteSource(private val client: HttpClient, private val json: Json) {

    suspend fun signup(email: String, password: String, username: String, nickname: String): LoginDataDto =
        apiCall(json) {
            client.post("/auth/signup") {
                contentType(ContentType.Application.Json)
                setBody(SignupBody(email, password, username, nickname))
            }
        }

    suspend fun login(email: String, password: String): LoginDataDto =
        apiCall(json) {
            client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginBody(email, password))
            }
        }

    suspend fun logout() {
        apiCallMessage(json) { client.post("/auth/logout") }
    }

    suspend fun verifyEmail(code: String): UserDto =
        apiCall(json) {
            client.post("/auth/verify-email") {
                contentType(ContentType.Application.Json)
                setBody(VerifyEmailBody(code))
            }
        }

    suspend fun resendVerification(email: String): String =
        apiCallMessage(json) {
            client.post("/auth/resend-verification") {
                contentType(ContentType.Application.Json)
                setBody(EmailBody(email))
            }
        }
}
