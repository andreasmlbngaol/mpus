package id.andreasmlbngaol.mpus.data

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class ApiException(val code: Int, override val message: String) : Exception(message)

/**
 * Thin OkHttp client for the MPUS backend.
 *
 * Response envelope: {success, message?, data?}. Note that axum's own extractor
 * failures (400/413/422/404/405) come back as *plain text*, not JSON — handled here.
 *
 * `open` on purpose: unit tests subclass it to stub the network without touching
 * OkHttp (the constructor's client is never used by an overriding fake).
 */
open class ApiClient(
    private val baseUrl: String,
    private val session: Session,
) {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    // ---- auth ---------------------------------------------------------------

    /** Create an account. Signs the new user in too, so the client lands on the verify screen. */
    open suspend fun signup(email: String, password: String, username: String, nickname: String): LoginData {
        val body = exec(
            post("/auth/signup", buildJsonObject {
                put("email", email); put("password", password)
                put("username", username); put("nickname", nickname)
            }.toString()),
        )
        return parse(body)
    }

    /** Redeem a verification code. Returns the updated user (`email_verified` now true). */
    open suspend fun verifyEmail(token: String): User =
        parse(exec(post("/auth/verify-email", buildJsonObject { put("token", token) }.toString())))

    open suspend fun resendVerification(email: String): String =
        message(exec(post("/auth/resend-verification", buildJsonObject { put("email", email) }.toString())))

    open suspend fun login(email: String, password: String): LoginData {
        val body = exec(
            post("/auth/login", buildJsonObject {
                put("email", email); put("password", password)
            }.toString()),
        )
        return parse(body)
    }

    open suspend fun logout() {
        exec(post("/auth/logout", null))
    }

    /** Register this device's FCM token so the backend can push to it. */
    open suspend fun registerDevice(token: String, platform: String = "android") {
        val body = buildJsonObject { put("token", token); put("platform", platform) }.toString()
        exec(post("/devices", body))
    }

    /** Forget this device's token — call on sign-out so a signed-out phone stops getting pushes. */
    open suspend fun unregisterDevice(token: String) {
        val body = buildJsonObject { put("token", token) }.toString()
        exec(post("/devices/delete", body))
    }

    // ---- profile ------------------------------------------------------------

    open suspend fun me(): User = parse(exec(get("/me")))

    open suspend fun patchMe(username: String?, nickname: String?): User {
        val body = buildJsonObject {
            username?.let { put("username", it) }
            nickname?.let { put("nickname", it) }
        }.toString()
        return parse(exec(request("/me", "PATCH", body.toRequestBody(jsonMedia))))
    }

    open suspend fun uploadAvatar(bytes: ByteArray, filename: String): User {
        val multipart = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("avatar", filename, bytes.toRequestBody("image/*".toMediaType()))
            .build()
        return parse(exec(request("/me/avatar", "POST", multipart)))
    }

    open suspend fun deleteAvatar(): User = parse(exec(delete("/me/avatar")))

    open suspend fun mySightings(cursor: String? = null): MySightings {
        val path = if (cursor == null) "/me/sightings" else "/me/sightings?cursor=$cursor"
        return parse(exec(get(path)))
    }

    open suspend fun userProfile(id: String, cursor: String? = null): UserProfile {
        val path = if (cursor == null) "/users/$id" else "/users/$id?cursor=$cursor"
        return parse(exec(get(path)))
    }

    // ---- sightings ----------------------------------------------------------

    open suspend fun createSighting(
        bytes: ByteArray,
        filename: String,
        lat: Double,
        lng: Double,
        takenAt: String?,
    ): SightingResult {
        val multipart = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("photo", filename, bytes.toRequestBody("image/*".toMediaType()))
            .addFormDataPart("lat", lat.toString())
            .addFormDataPart("lng", lng.toString())
            .apply { takenAt?.let { addFormDataPart("taken_at", it) } }
            .build()
        return parse(exec(request("/sightings", "POST", multipart)))
    }

    open suspend fun resolveSighting(id: String, catId: String?, name: String?): ResolveData {
        val body = buildJsonObject {
            catId?.let { put("cat_id", it) }
            name?.let { put("name", it) }
        }.toString()
        return parse(exec(post("/sightings/$id/resolve", body)))
    }

    // ---- cats ---------------------------------------------------------------

    open suspend fun cats(minLat: Double, minLng: Double, maxLat: Double, maxLng: Double): List<CatMarker> =
        parse(exec(get("/cats?min_lat=$minLat&min_lng=$minLng&max_lat=$maxLat&max_lng=$maxLng")))

    open suspend fun catDetail(id: String): CatDetail = parse(exec(get("/cats/$id")))

    open suspend fun setName(catId: String, name: String): CatDetail =
        parse(exec(post("/cats/$catId/names", buildJsonObject { put("name", name) }.toString())))

    open suspend fun like(nameId: String): LikeData = parse(exec(post("/names/$nameId/like", null)))

    open suspend fun setReview(catId: String, body: String, rating: Int): CatDetail =
        parse(
            exec(
                post(
                    "/cats/$catId/reviews",
                    buildJsonObject { put("body", body); put("rating", rating) }.toString(),
                ),
            ),
        )

    open suspend fun likeReview(reviewId: String): LikeData =
        parse(exec(post("/reviews/$reviewId/like", null)))

    // ---- moderation ---------------------------------------------------------

    /** Flag a name ("name") or review ("review"). Hides itself once enough people agree. */
    open suspend fun report(targetKind: String, targetId: String, reason: String? = null): ReportResult {
        val body = buildJsonObject {
            put("target_kind", targetKind); put("target_id", targetId)
            reason?.let { put("reason", it) }
        }.toString()
        return parse(exec(post("/reports", body)))
    }

    // ---- notifications ------------------------------------------------------

    open suspend fun notifications(cursor: String? = null): Page<AppNotification> {
        val path = if (cursor == null) "/notifications" else "/notifications?cursor=$cursor"
        return parse(exec(get(path)))
    }

    open suspend fun unreadCount(): UnreadCount = parse(exec(get("/notifications/unread-count")))

    open suspend fun markNotificationsSeen() {
        exec(post("/notifications/seen", null))
    }

    // ---- merge --------------------------------------------------------------

    /** Ask to fold [sourceCatId] into [targetCatId]. Both owners must agree when they differ. */
    open suspend fun requestMerge(sourceCatId: String, targetCatId: String): MergeRequest {
        val body = buildJsonObject { put("target_cat_id", targetCatId) }.toString()
        return parse(exec(post("/cats/$sourceCatId/merge", body)))
    }

    open suspend fun pendingMerges(): List<MergeRequest> = parse(exec(get("/merges/pending")))

    open suspend fun approveMerge(id: String): MergeRequest = parse(exec(post("/merges/$id/approve", null)))

    open suspend fun rejectMerge(id: String) {
        exec(post("/merges/$id/reject", null))
    }

    // ---- plumbing -----------------------------------------------------------

    private fun get(path: String) = request(path, "GET", null)

    private fun post(path: String, body: String?) =
        // OkHttp rejects a POST with a null body outright, so body-less POSTs (likes,
        // logout) go out with an empty one.
        request(path, "POST", (body ?: "").toRequestBody(jsonMedia))

    private fun delete(path: String) = request(path, "DELETE", null)

    private fun request(path: String, method: String, body: RequestBody?): Request {
        val b = Request.Builder().url("$baseUrl$path").method(method, body)
        session.token.value?.let { b.header("Authorization", "Bearer $it") }
        return b.build()
    }

    private suspend fun exec(request: Request): String = withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { resp ->
            val body = resp.body.string()
            if (!resp.isSuccessful) throw ApiException(resp.code, errorMessage(body))
            body
        }
    }

    private fun errorMessage(body: String): String =
        runCatching { json.decodeFromString<MessageEnvelope>(body).message }.getOrNull()
            ?: body.takeIf { it.isNotBlank() }?.take(300)
            ?: "Something went wrong."

    private inline fun <reified T> parse(body: String): T {
        val env = json.decodeFromString<Envelope<T>>(body)
        if (!env.success) throw ApiException(200, env.message ?: "Request failed.")
        return env.data ?: throw ApiException(200, "No data returned.")
    }

    private fun message(body: String): String {
        val env = json.decodeFromString<MessageEnvelope>(body)
        if (!env.success) throw ApiException(200, env.message ?: "Request failed.")
        return env.message.orEmpty()
    }
}
