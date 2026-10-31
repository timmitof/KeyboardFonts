package kg.timmitof.feature_settings.presentation.studio

import android.net.Uri
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.PlatformFile

internal actual fun PlatformFile.photoUri(): String = when (val file = androidFile) {
    is AndroidFile.UriWrapper -> file.uri.toString()
    is AndroidFile.FileWrapper -> Uri.fromFile(file.file).toString()
}
