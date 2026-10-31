package kg.timmitof.feature_settings.presentation.studio

import io.github.vinceglb.filekit.PlatformFile

/** Ссылка на выбранное фото для хранилища фонов: `content://` на Android, `file://` на iOS. */
internal expect fun PlatformFile.photoUri(): String
