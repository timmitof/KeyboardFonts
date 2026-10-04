package kg.timmitof.core.navigation.graphs

import kotlinx.serialization.Serializable

@Serializable
data object HomeGraph {

    /**
     * Подключение клавиатуры: три шага и проба шрифтов.
     *
     * @property isFromSettings экран открыт из «Студии» — по «Готово» возвращаемся
     * назад, а не открываем её заново поверх.
     */
    @Serializable
    data class OnboardingScreen(val isFromSettings: Boolean = false)
}
