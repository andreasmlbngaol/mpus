package id.andreasmlbngaol.mpus.core.di

import android.content.Context
import id.andreasmlbngaol.mpus.BuildConfig
import id.andreasmlbngaol.mpus.core.data.deeplink.DeepLinkRepositoryImpl
import id.andreasmlbngaol.mpus.core.data.device.DeviceRemoteSource
import id.andreasmlbngaol.mpus.core.data.device.DeviceRepositoryImpl
import id.andreasmlbngaol.mpus.core.data.location.LocationRepositoryImpl
import id.andreasmlbngaol.mpus.core.data.network.createHttpClient
import id.andreasmlbngaol.mpus.core.data.network.createJson
import id.andreasmlbngaol.mpus.core.data.push.PushRegistrar
import id.andreasmlbngaol.mpus.core.data.session.SessionRepositoryImpl
import id.andreasmlbngaol.mpus.core.domain.repository.DeepLinkRepository
import id.andreasmlbngaol.mpus.core.domain.repository.DeviceRepository
import id.andreasmlbngaol.mpus.core.domain.repository.LocationRepository
import id.andreasmlbngaol.mpus.core.domain.repository.PushRepository
import id.andreasmlbngaol.mpus.core.domain.repository.SessionRepository
import id.andreasmlbngaol.mpus.core.domain.usecase.SessionUseCase
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Everything shared across features: the JSON config, the one Ktor client, the session,
 * deep-link and location surfaces, and push registration. Externally-constructed objects
 * (base URL, client) get providers here; the rest is wired by constructor.
 */
@Module
class CoreModule {

    @Single
    fun json(): Json = createJson()

    @Single
    fun httpClient(session: SessionRepository, json: Json): HttpClient =
        createHttpClient(BuildConfig.API_BASE_URL, session, json)

    @Single
    fun sessionRepository(context: Context, json: Json): SessionRepository =
        SessionRepositoryImpl(context, json)

    @Single
    fun deepLinkRepository(): DeepLinkRepository = DeepLinkRepositoryImpl()

    @Single
    fun locationRepository(context: Context): LocationRepository = LocationRepositoryImpl(context)

    @Single
    fun deviceRepository(client: HttpClient, json: Json): DeviceRepository =
        DeviceRepositoryImpl(DeviceRemoteSource(client, json))

    @Single
    fun pushRegistrar(devices: DeviceRepository, session: SessionRepository): PushRepository =
        PushRegistrar(devices, session)

    @Single
    fun sessionUseCase(session: SessionRepository): SessionUseCase = SessionUseCase(session)
}
