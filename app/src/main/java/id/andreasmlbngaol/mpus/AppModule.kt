package id.andreasmlbngaol.mpus

import id.andreasmlbngaol.mpus.auth.di.AuthModule
import id.andreasmlbngaol.mpus.cat.di.CatModule
import id.andreasmlbngaol.mpus.core.di.CoreModule
import id.andreasmlbngaol.mpus.map.di.MapModule
import id.andreasmlbngaol.mpus.notifications.di.NotificationsModule
import id.andreasmlbngaol.mpus.profile.di.ProfileModule
import id.andreasmlbngaol.mpus.sighting.di.SightingModule
import org.koin.core.annotation.Module

/**
 * The root module. Each feature owns its own module (with a `@ComponentScan` over its own
 * package); this one just pulls them all together for `@KoinApplication`.
 */
@Module(
    includes = [
        CoreModule::class,
        AuthModule::class,
        MapModule::class,
        CatModule::class,
        SightingModule::class,
        ProfileModule::class,
        NotificationsModule::class,
    ],
)
class AppModule
