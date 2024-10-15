package com.timmitof.keyboardfonts.ui.toolbar

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timmitof.common.toolbar.ToolbarMode
import com.timmitof.common.toolbar.ToolbarState
import com.timmitof.common.R

/**
 * Компонент верхней навигационной панели
 */
@Composable
fun Toolbar(
    modifier: Modifier = Modifier, toolbarState: State<ToolbarState>
) {
    if (toolbarState.value.isVisible) {
        Crossfade(targetState = toolbarState.value.mode, label = "") { mode ->
            Column(modifier = modifier) {
                when (mode) {
                    is ToolbarMode.Main -> MainToolbar(mode = mode)
                    is ToolbarMode.None -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun MainToolbar(
    modifier: Modifier = Modifier, mode: ToolbarMode.Main = ToolbarMode.Main()
) {
    CenterAlignedTopAppBar(modifier = modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        title = {},
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.Transparent),
        actions = {
            IconButton(onClick = mode.onKeyboardTutorial) {
                val keyboardIcon =
                    if (mode.keyboardIsOn.value) painterResource(id = R.drawable.ic_keyboard_on)
                    else painterResource(id = R.drawable.ic_keyboard_off)

                Image(
                    painter = keyboardIcon,
                    contentDescription = "Keyboard Tutorial"
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(onClick = mode.onSettings) {
                Image(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings"
                )
            }
        }
    )
}