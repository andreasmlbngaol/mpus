package id.andreasmlbngaol.mpus.di

import android.content.Context
import id.andreasmlbngaol.mpus.BuildConfig
import id.andreasmlbngaol.mpus.data.ApiClient
import id.andreasmlbngaol.mpus.data.PushRegistrar
import id.andreasmlbngaol.mpus.data.Session
import id.andreasmlbngaol.mpus.data.SessionStore
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Single Koin module. `@ComponentScan` picks up every `@Single` / `@Factory` /
 * `@KoinViewModel` in the app package, so most classes need no entry here.
 * Only externally-constructed objects (base URL, OkHttp-backed client) get providers.
 */
@Module
@ComponentScan("id.andreasmlbngaol.mpus.ui")
class AppModule {

    @Single
    fun apiClient(session: Session): ApiClient = ApiClient(BuildConfig.API_BASE_URL, session)

    @Single
    fun sessionStore(context: Context): SessionStore = SessionStore(context)

    /** ViewModels depend on the [Session] interface; back it with the real store. */
    @Single
    fun session(store: SessionStore): Session = store

    @Single
    fun pushRegistrar(api: ApiClient, session: Session): PushRegistrar = PushRegistrar(api, session)
}
