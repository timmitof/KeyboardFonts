package com.timmitof.keyboardfonts.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.timmitof.common.navigation.NavigationLinks
import com.timmitof.common.toolbar.ToolbarState

/**
 * Навигация проекта
 */
@Composable
internal fun MainNavHost(
    modifier: Modifier = Modifier,
    toolbarState: MutableState<ToolbarState> = remember { mutableStateOf(ToolbarState()) },
    contentPadding: PaddingValues = PaddingValues(),
    navHostController: NavHostController = rememberNavController(),
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
) {
    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = NavigationLinks.Home
    ) {
        composable<NavigationLinks.Home> {

        }
    }
}