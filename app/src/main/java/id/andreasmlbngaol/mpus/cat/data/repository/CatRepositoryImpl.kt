package id.andreasmlbngaol.mpus.cat.data.repository

import id.andreasmlbngaol.mpus.cat.data.mapper.toDomain
import id.andreasmlbngaol.mpus.cat.data.source.CatRemoteSource
import id.andreasmlbngaol.mpus.cat.domain.model.CatDetail
import id.andreasmlbngaol.mpus.cat.domain.model.MergeTarget
import id.andreasmlbngaol.mpus.cat.domain.repository.CatRepository
import id.andreasmlbngaol.mpus.core.data.mapper.toDomain
import id.andreasmlbngaol.mpus.core.domain.model.MergeRequest

class CatRepositoryImpl(private val source: CatRemoteSource) : CatRepository {

    override suspend fun detail(id: String): CatDetail = source.detail(id).toDomain()

    override suspend fun setName(catId: String, name: String): CatDetail = source.setName(catId, name).toDomain()

    override suspend fun likeName(nameId: String) {
        source.likeName(nameId)
    }

    override suspend fun setReview(catId: String, body: String, rating: Int): CatDetail =
        source.setReview(catId, body, rating).toDomain()

    override suspend fun likeReview(reviewId: String) {
        source.likeReview(reviewId)
    }

    override suspend fun report(targetKind: String, targetId: String): Boolean =
        source.report(targetKind, targetId).hidden

    override suspend fun mergeTargets(excludeCatId: String): List<MergeTarget> {
        val mine = source.mySightings().items.mapNotNull { it.catId }.distinct().filter { it != excludeCatId }
        return mine.mapNotNull { id ->
            runCatching { source.detail(id) }.getOrNull()?.let {
                MergeTarget(it.id, it.displayName, it.sightings.firstOrNull()?.thumbUrl)
            }
        }
    }

    override suspend fun requestMerge(sourceCatId: String, targetCatId: String): MergeRequest =
        source.requestMerge(sourceCatId, targetCatId).toDomain()
}
