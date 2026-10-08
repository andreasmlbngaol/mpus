package id.andreasmlbngaol.mpus.cat.di

import id.andreasmlbngaol.mpus.cat.data.repository.CatRepositoryImpl
import id.andreasmlbngaol.mpus.cat.data.source.CatRemoteSource
import id.andreasmlbngaol.mpus.cat.domain.repository.CatRepository
import id.andreasmlbngaol.mpus.cat.domain.usecase.CatUseCase
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("id.andreasmlbngaol.mpus.cat")
class CatModule {

    @Single
    fun catRemoteSource(client: HttpClient, json: Json): CatRemoteSource = CatRemoteSource(client, json)

    @Single
    fun catRepository(source: CatRemoteSource): CatRepository = CatRepositoryImpl(source)

    @Single
    fun catUseCase(repo: CatRepository): CatUseCase = CatUseCase(repo)
}
