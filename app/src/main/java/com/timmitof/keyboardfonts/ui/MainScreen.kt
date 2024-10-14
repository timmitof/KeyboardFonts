package com.timmitof.keyboardfonts.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.timmitof.common.toolbar.ToolbarMode
import com.timmitof.common.toolbar.ToolbarState
import com.timmitof.keyboardfonts.ui.toolbar.Toolbar

/**
 * Стартовый экран
 */
@Composable
internal fun MainScreen(
    navHostController: NavHostController = rememberNavController(),
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact
) {
    val toolbarState = remember { mutableStateOf(ToolbarState(mode = ToolbarMode.Main())) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Toolbar(
                modifier = Modifier.background(Color.Transparent),
                toolbarState = toolbarState
            )
        },
        content = { contentPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize(),
                content = {
                    MainNavHost(
                        modifier = Modifier.fillMaxSize(),
                        toolbarState = toolbarState,
                        contentPadding = contentPadding,
                        navHostController = navHostController,
                        windowWidthSizeClass = windowWidthSizeClass
                    )
                }
            )
        }
    )
}