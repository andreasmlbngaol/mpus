package id.andreasmlbngaol.mpus

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin

/**
 * Koin entry point. The Compiler Plugin turns [startKoin] into a typed call that
 * loads every module discovered from this annotation — no generated code to import.
 */
@KoinApplication(modules = [AppModule::class])
class MpusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin<MpusApplication> {
            androidContext(this@MpusApplication)
        }

        // Create the notification channel up front: a background push is drawn by the
        // system before any Activity runs, and it needs the channel to already exist (or
        // it lands in the system's own "Miscellaneous" channel instead of ours).
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
    }

    companion object {
        const val CHANNEL_ID = "mpus_notifications"
    }
}
