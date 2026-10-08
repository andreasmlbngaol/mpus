package id.andreasmlbngaol.mpus.profile.di

import id.andreasmlbngaol.mpus.profile.data.repository.ProfileRepositoryImpl
import id.andreasmlbngaol.mpus.profile.data.repository.UserRepositoryImpl
import id.andreasmlbngaol.mpus.profile.data.source.ProfileRemoteSource
import id.andreasmlbngaol.mpus.profile.domain.repository.ProfileRepository
import id.andreasmlbngaol.mpus.profile.domain.repository.UserRepository
import id.andreasmlbngaol.mpus.profile.domain.usecase.ProfileUseCase
import id.andreasmlbngaol.mpus.profile.domain.usecase.UserUseCase
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("id.andreasmlbngaol.mpus.profile")
class ProfileModule {

    @Single
    fun profileRemoteSource(client: HttpClient, json: Json): ProfileRemoteSource =
        ProfileRemoteSource(client, json)

    @Single
    fun profileRepository(source: ProfileRemoteSource): ProfileRepository = ProfileRepositoryImpl(source)

    @Single
    fun userRepository(source: ProfileRemoteSource): UserRepository = UserRepositoryImpl(source)

    @Single
    fun profileUseCase(repo: ProfileRepository): ProfileUseCase = ProfileUseCase(repo)

    @Single
    fun userUseCase(repo: UserRepository): UserUseCase = UserUseCase(repo)
}
