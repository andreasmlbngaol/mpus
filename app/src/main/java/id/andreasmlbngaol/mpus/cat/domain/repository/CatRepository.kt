package id.andreasmlbngaol.mpus.cat.domain.repository

import id.andreasmlbngaol.mpus.cat.domain.model.CatDetail
import id.andreasmlbngaol.mpus.cat.domain.model.MergeTarget
import id.andreasmlbngaol.mpus.core.domain.model.MergeRequest

/** The cat detail page's data: read it, name it, like, review, report, and merge. */
interface CatRepository {
    suspend fun detail(id: String): CatDetail
    suspend fun setName(catId: String, name: String): CatDetail
    suspend fun likeName(nameId: String)
    suspend fun setReview(catId: String, body: String, rating: Int): CatDetail
    suspend fun likeReview(reviewId: String)

    /** Flag a name ("name") or review ("review"). Returns true if it pushed the item over the hide threshold. */
    suspend fun report(targetKind: String, targetId: String): Boolean

    /** The cats this viewer could merge into: their own sightings' cats, minus this one. */
    suspend fun mergeTargets(excludeCatId: String): List<MergeTarget>

    /** Ask to fold this cat into [targetCatId]. Returns the resulting merge request. */
    suspend fun requestMerge(sourceCatId: String, targetCatId: String): MergeRequest
}
