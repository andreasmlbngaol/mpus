package id.andreasmlbngaol.mpus.sighting.di

import id.andreasmlbngaol.mpus.sighting.data.repository.SightingRepositoryImpl
import id.andreasmlbngaol.mpus.sighting.data.source.SightingRemoteSource
import id.andreasmlbngaol.mpus.sighting.domain.repository.SightingRepository
import id.andreasmlbngaol.mpus.sighting.domain.usecase.SightingUseCase
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("id.andreasmlbngaol.mpus.sighting")
class SightingModule {

    @Single
    fun sightingRemoteSource(client: HttpClient, json: Json): SightingRemoteSource =
        SightingRemoteSource(client, json)

    @Single
    fun sightingRepository(source: SightingRemoteSource): SightingRepository = SightingRepositoryImpl(source)

    @Single
    fun sightingUseCase(repo: SightingRepository): SightingUseCase = SightingUseCase(repo)
}
