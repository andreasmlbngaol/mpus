package id.andreasmlbngaol.mpus

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import id.andreasmlbngaol.mpus.core.domain.repository.DeepLinkRepository
import id.andreasmlbngaol.mpus.MpusApp
import id.andreasmlbngaol.mpus.core.ui.theme.MPUSTheme
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {
    private val deepLinks: DeepLinkRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MPUSTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MpusApp()
                }
            }
        }
        routeNotification(intent)
    }

    // singleTop: a notification tap while the app is already open lands here instead of
    // building a second activity, so the extra survives as a deep-link target.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        routeNotification(intent)
    }

    /**
     * A tapped FCM notification carries the payload's data as intent extras (the SDK does
     * this for background pushes; our own foreground notification sets them explicitly).
     * Hand the target to [DeepLinkRepository], which the nav layer consumes once signed in.
     */
    private fun routeNotification(intent: Intent?) {
        val catId = intent?.getStringExtra("cat_id")
        val kind = intent?.getStringExtra("kind")
        if (catId != null || kind != null) {
            deepLinks.post(kind, catId)
        }
    }
}
