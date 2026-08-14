package kg.timmitof.core.navigation.graphs

import kotlinx.serialization.Serializable

@Serializable
data object HomeGraph {

    /** Проверка клавиатуры: инструкция подключения и тестовые поля ввода. */
    @Serializable
    data object CheckKeyboardScreen
}
