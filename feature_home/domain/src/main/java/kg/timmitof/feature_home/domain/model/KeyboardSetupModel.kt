package kg.timmitof.feature_home.domain.model

/**
 * Шаг подключения клавиатуры: сначала её нужно включить в системе, затем выбрать текущей.
 *
 * [DONE] — оба шага пройдены, клавиатура готова к работе.
 */
enum class KeyboardSetupStep {
    ENABLE,
    SELECT,
    DONE
}

/**
 * Состояние подключения клавиатуры.
 *
 * @property isEnabled клавиатура включена в списке методов ввода.
 * @property isSelected клавиатура выбрана текущим методом ввода.
 */
data class KeyboardSetupModel(
    val isEnabled: Boolean = false,
    val isSelected: Boolean = false
) {
    /** Первый непройденный шаг — на нём и держим фокус пользователя. */
    val currentStep: KeyboardSetupStep
        get() = when {
            !isEnabled -> KeyboardSetupStep.ENABLE
            !isSelected -> KeyboardSetupStep.SELECT
            else -> KeyboardSetupStep.DONE
        }
}
