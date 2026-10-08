package id.andreasmlbngaol.mpus.map.di

import id.andreasmlbngaol.mpus.map.data.repository.MapRepositoryImpl
import id.andreasmlbngaol.mpus.map.data.source.MapRemoteSource
import id.andreasmlbngaol.mpus.map.domain.repository.MapRepository
import id.andreasmlbngaol.mpus.map.domain.usecase.MapUseCase
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("id.andreasmlbngaol.mpus.map")
class MapModule {

    @Single
    fun mapRemoteSource(client: HttpClient, json: Json): MapRemoteSource = MapRemoteSource(client, json)

    @Single
    fun mapRepository(source: MapRemoteSource): MapRepository = MapRepositoryImpl(source)

    @Single
    fun mapUseCase(repo: MapRepository): MapUseCase = MapUseCase(repo)
}
