package com.timmitof.common.toolbar

import androidx.compose.runtime.MutableState

/**
 * Состояние тулбара
 */
data class ToolbarState(
    val isVisible: Boolean = true,
    val mode: ToolbarMode = ToolbarMode.Main()
) {
    companion object {

        fun MutableState<ToolbarState>.isVisible(value: Boolean) {
            this.value = this.value.copy(isVisible = value)
        }

        fun MutableState<ToolbarState>.mode(value: ToolbarMode) {
            if(this.value.mode::class == value::class) return
            this.value = this.value.copy(mode = value)
        }
    }
}