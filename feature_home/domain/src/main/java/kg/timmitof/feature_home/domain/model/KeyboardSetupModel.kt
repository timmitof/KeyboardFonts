package kg.timmitof.feature_home.domain.model

enum class KeyboardSetupStep {
    ENABLE,
    SELECT,
    DONE
}

data class KeyboardSetupModel(
    val isEnabled: Boolean = false,
    val isSelected: Boolean = false
) {
    val currentStep: KeyboardSetupStep
        get() = when {
            !isEnabled -> KeyboardSetupStep.ENABLE
            !isSelected -> KeyboardSetupStep.SELECT
            else -> KeyboardSetupStep.DONE
        }
}
