package kg.timmitof.keyboard.presentation.screens.keyboard.states

import KeyboardFonts.keyboard.keyboard.presentation.R
import androidx.annotation.DrawableRes

internal enum class ShiftState(@param:DrawableRes val icon: Int) {
    DISABLED(icon = R.drawable.ic_shift_key),
    ACTIVE(icon = R.drawable.ic_shift_filled_key),
    CAPS_LOCK(icon = R.drawable.ic_caps_key)
}

internal fun ShiftState.isUpperCase(): Boolean {
    return this == ShiftState.ACTIVE || this == ShiftState.CAPS_LOCK
}