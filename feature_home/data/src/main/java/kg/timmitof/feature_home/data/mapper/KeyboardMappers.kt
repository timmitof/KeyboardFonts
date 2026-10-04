package kg.timmitof.feature_home.data.mapper

import kg.timmitof.feature_home.domain.model.KeyboardSetupModel
import kg.timmitof.keyboard.integration.KeyboardState

fun KeyboardState.toDomain() = KeyboardSetupModel(
    isEnabled = this.isEnabled,
    isSelected = this.isSelected
)
