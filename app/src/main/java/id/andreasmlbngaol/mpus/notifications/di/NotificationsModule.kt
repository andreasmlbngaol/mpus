package id.andreasmlbngaol.mpus.notifications.di

import id.andreasmlbngaol.mpus.notifications.data.repository.NotificationsRepositoryImpl
import id.andreasmlbngaol.mpus.notifications.data.source.NotificationsRemoteSource
import id.andreasmlbngaol.mpus.notifications.domain.repository.NotificationsRepository
import id.andreasmlbngaol.mpus.notifications.domain.usecase.NotificationsUseCase
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("id.andreasmlbngaol.mpus.notifications")
class NotificationsModule {

    @Single
    fun notificationsRemoteSource(client: HttpClient, json: Json): NotificationsRemoteSource =
        NotificationsRemoteSource(client, json)

    @Single
    fun notificationsRepository(source: NotificationsRemoteSource): NotificationsRepository =
        NotificationsRepositoryImpl(source)

    @Single
    fun notificationsUseCase(repo: NotificationsRepository): NotificationsUseCase = NotificationsUseCase(repo)
}
