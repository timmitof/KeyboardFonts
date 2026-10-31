package kg.timmitof.core.navigation.graphs

import kotlinx.serialization.Serializable

@Serializable
data object SettingsGraph {

    @Serializable
    data object SettingsScreen

    /** [photoId] — фото из базы; экран выставляет, какая его часть видна на клавиатуре. */
    @Serializable
    data class BackgroundCropScreen(val photoId: Long)
}
