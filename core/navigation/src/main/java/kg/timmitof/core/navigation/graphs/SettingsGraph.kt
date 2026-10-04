package kg.timmitof.core.navigation.graphs

import kotlinx.serialization.Serializable

@Serializable
data object SettingsGraph {

    @Serializable
    data object SettingsScreen

    /** [uri] — content:// из системного выбора фото. */
    @Serializable
    data class BackgroundCropScreen(val uri: String)
}
