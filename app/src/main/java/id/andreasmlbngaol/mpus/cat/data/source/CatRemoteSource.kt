package id.andreasmlbngaol.mpus.cat.data.source

import id.andreasmlbngaol.mpus.cat.data.dto.CatDetailDto
import id.andreasmlbngaol.mpus.cat.data.dto.LikeDataDto
import id.andreasmlbngaol.mpus.cat.data.dto.MySightingsDto
import id.andreasmlbngaol.mpus.cat.data.dto.ReportResultDto
import id.andreasmlbngaol.mpus.core.data.dto.MergeRequestDto
import id.andreasmlbngaol.mpus.core.data.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class NameBody(val name: String)

@Serializable
private data class ReviewBody(val body: String, val rating: Int)

@Serializable
private data class ReportBody(
    @kotlinx.serialization.SerialName("target_kind") val targetKind: String,
    @kotlinx.serialization.SerialName("target_id") val targetId: String,
)

@Serializable
private data class MergeBody(@kotlinx.serialization.SerialName("target_cat_id") val targetCatId: String)

class CatRemoteSource(private val client: HttpClient, private val json: Json) {

    suspend fun detail(id: String): CatDetailDto = apiCall(json) { client.get("/cats/$id") }

    suspend fun setName(catId: String, name: String): CatDetailDto =
        apiCall(json) {
            client.post("/cats/$catId/names") {
                contentType(ContentType.Application.Json)
                setBody(NameBody(name))
            }
        }

    suspend fun likeName(nameId: String): LikeDataDto =
        apiCall(json) { client.post("/names/$nameId/like") }

    suspend fun setReview(catId: String, body: String, rating: Int): CatDetailDto =
        apiCall(json) {
            client.post("/cats/$catId/reviews") {
                contentType(ContentType.Application.Json)
                setBody(ReviewBody(body, rating))
            }
        }

    suspend fun likeReview(reviewId: String): LikeDataDto =
        apiCall(json) { client.post("/reviews/$reviewId/like") }

    suspend fun report(targetKind: String, targetId: String): ReportResultDto =
        apiCall(json) {
            client.post("/reports") {
                contentType(ContentType.Application.Json)
                setBody(ReportBody(targetKind, targetId))
            }
        }

    suspend fun mySightings(): MySightingsDto = apiCall(json) { client.get("/me/sightings") }

    suspend fun requestMerge(sourceCatId: String, targetCatId: String): MergeRequestDto =
        apiCall(json) {
            client.post("/cats/$sourceCatId/merge") {
                contentType(ContentType.Application.Json)
                setBody(MergeBody(targetCatId))
            }
        }
}
