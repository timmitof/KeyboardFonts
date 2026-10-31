package kg.timmitof.feature_settings.presentation.studio

import io.github.vinceglb.filekit.PlatformFile

// FileKit уже скопировал файл из PHPicker: его временная ссылка живёт только внутри колбэка.
internal actual fun PlatformFile.photoUri(): String = requireNotNull(nsUrl.absoluteString) { "Photo URL is unavailable" }
