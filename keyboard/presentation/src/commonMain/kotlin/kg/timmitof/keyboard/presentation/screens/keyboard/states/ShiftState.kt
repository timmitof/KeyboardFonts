package kg.timmitof.keyboard.presentation.screens.keyboard.states

import kg.timmitof.keyboard.presentation.resources.Res
import kg.timmitof.keyboard.presentation.resources.ic_caps_key
import kg.timmitof.keyboard.presentation.resources.ic_shift_filled_key
import kg.timmitof.keyboard.presentation.resources.ic_shift_key
import org.jetbrains.compose.resources.DrawableResource

internal enum class ShiftState(val icon: DrawableResource) {
    DISABLED(icon = Res.drawable.ic_shift_key),
    ACTIVE(icon = Res.drawable.ic_shift_filled_key),
    CAPS_LOCK(icon = Res.drawable.ic_caps_key)
}

internal fun ShiftState.isUpperCase(): Boolean {
    return this == ShiftState.ACTIVE || this == ShiftState.CAPS_LOCK
}