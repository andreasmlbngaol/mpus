package id.andreasmlbngaol.mpus.core.ui.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object CaptureFiles {
    /** A fresh cache file + its content Uri, ready to hand to the camera intent. */
    fun newImage(context: Context): Pair<File, Uri> {
        val dir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return file to uri
    }
}
