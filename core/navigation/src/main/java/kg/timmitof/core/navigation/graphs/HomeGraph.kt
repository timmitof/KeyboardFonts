package kg.timmitof.core.navigation.graphs

import kotlinx.serialization.Serializable

@Serializable
data object HomeGraph {

    /** isFromSettings: по «Готово» возвращаемся назад, а не открываем «Студию» заново. */
    @Serializable
    data class OnboardingScreen(val isFromSettings: Boolean = false)
}
