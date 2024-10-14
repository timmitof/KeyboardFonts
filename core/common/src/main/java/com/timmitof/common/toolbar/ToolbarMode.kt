package com.timmitof.common.toolbar

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

/**
 * Версии отображения тулбара
 */
sealed interface ToolbarMode {

    data object None: ToolbarMode

    data class Main(
        val keyboardIsOn: State<Boolean> = mutableStateOf(false),
        val onKeyboardTutorial: () -> Unit = {},
        val onSettings: () -> Unit = {}
    ): ToolbarMode

}