package kg.timmitof.feature_settings.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kg.timmitof.core.navigation.graphs.SettingsGraph
import kg.timmitof.feature_settings.presentation.screens.SettingsScreen

fun NavGraphBuilder.settingsGraph() {
    navigation<SettingsGraph>(startDestination = SettingsGraph.SettingsScreen) {
        composable<SettingsGraph.SettingsScreen> {
            SettingsScreen()
        }
    }
}
