package id.andreasmlbngaol.mpus.cat.domain.usecase

import id.andreasmlbngaol.mpus.cat.domain.model.CatDetail
import id.andreasmlbngaol.mpus.cat.domain.model.MergeTarget
import id.andreasmlbngaol.mpus.cat.domain.repository.CatRepository
import id.andreasmlbngaol.mpus.core.domain.model.MergeRequest

/** Everything the cat detail page does. One class, several methods — all the same purpose. */
class CatUseCase(private val repo: CatRepository) {
    suspend fun detail(id: String): CatDetail = repo.detail(id)
    suspend fun setName(catId: String, name: String): CatDetail = repo.setName(catId, name)
    suspend fun likeName(nameId: String) = repo.likeName(nameId)
    suspend fun setReview(catId: String, body: String, rating: Int): CatDetail = repo.setReview(catId, body, rating)
    suspend fun likeReview(reviewId: String) = repo.likeReview(reviewId)
    suspend fun report(kind: String, targetId: String): Boolean = repo.report(kind, targetId)
    suspend fun mergeTargets(excludeCatId: String): List<MergeTarget> = repo.mergeTargets(excludeCatId)
    suspend fun requestMerge(sourceCatId: String, targetCatId: String): MergeRequest =
        repo.requestMerge(sourceCatId, targetCatId)
}
