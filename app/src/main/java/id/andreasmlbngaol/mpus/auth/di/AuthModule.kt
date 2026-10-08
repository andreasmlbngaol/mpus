package id.andreasmlbngaol.mpus.auth.di

import id.andreasmlbngaol.mpus.auth.data.repository.AuthRepositoryImpl
import id.andreasmlbngaol.mpus.auth.data.source.AuthRemoteSource
import id.andreasmlbngaol.mpus.auth.domain.repository.AuthRepository
import id.andreasmlbngaol.mpus.auth.domain.usecase.AuthUseCase
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("id.andreasmlbngaol.mpus.auth")
class AuthModule {

    @Single
    fun authRemoteSource(client: HttpClient, json: Json): AuthRemoteSource = AuthRemoteSource(client, json)

    @Single
    fun authRepository(source: AuthRemoteSource): AuthRepository = AuthRepositoryImpl(source)

    @Single
    fun authUseCase(auth: AuthRepository): AuthUseCase = AuthUseCase(auth)
}
