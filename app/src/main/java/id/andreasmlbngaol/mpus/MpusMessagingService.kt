package id.andreasmlbngaol.mpus

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import id.andreasmlbngaol.mpus.core.domain.repository.PushRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * FCM entry points. Two jobs:
 *
 *  - [onNewToken]: hand a rotated token to [PushRegistrar] so the backend stays pointed
 *    at this device.
 *  - [onMessageReceived]: when the app is in the foreground the system does *not* draw a
 *    notification itself, so we draw it here. In the background the system handles it and
 *    this is not called. Either way the in-app inbox picks the row up on its next poll.
 *
 * Both callbacks are flagged deprecated in firebase-messaging 25.x with no public
 * replacement, but they are still the only hooks — hence the suppressions.
 */
class MpusMessagingService : FirebaseMessagingService(), KoinComponent {
    private val registrar: PushRepository by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onNewToken(token: String) {
        scope.launch { registrar.onTokenRefreshed(token) }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Suppress("DEPRECATION")
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: getString(R.string.app_name)
        val body = message.notification?.body ?: return
        show(title, body, message.data["kind"], message.data["cat_id"])
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun show(title: String, body: String, kind: String?, catId: String?) {
        // Channel is created in MpusApplication, which always runs before this service.

        // Tapping re-enters MainActivity with the target as extras; a unique request code
        // keeps two stacked notifications from sharing one PendingIntent (and its extras).
        val id = System.currentTimeMillis().toInt()
        val tap = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("kind", kind)
            putExtra("cat_id", catId)
        }
        val contentIntent = PendingIntent.getActivity(
            this,
            id,
            tap,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, MpusApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        // A missing POST_NOTIFICATIONS grant makes this a silent no-op, which is fine.
        NotificationManagerCompat.from(this).notify(id, notification)
    }
}
