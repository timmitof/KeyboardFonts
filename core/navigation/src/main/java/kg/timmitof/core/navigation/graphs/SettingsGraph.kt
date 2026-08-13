package kg.timmitof.core.navigation.graphs

import kotlinx.serialization.Serializable

/** Настройки клавиатуры — корневой экран приложения. */
@Serializable
data object SettingsGraph {

    @Serializable
    data object SettingsScreen
}
