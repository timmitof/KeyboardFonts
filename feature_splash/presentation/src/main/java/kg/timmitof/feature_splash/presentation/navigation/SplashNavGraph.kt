package kg.timmitof.feature_splash.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kg.timmitof.core.navigation.graphs.SplashGraph
import kg.timmitof.feature_splash.presentation.screens.SplashScreen

fun NavGraphBuilder.splashGraph() {
   navigation<SplashGraph>(startDestination = SplashGraph.SplashScreen) {
       composable<SplashGraph.SplashScreen> {
           SplashScreen()
       }
   }
}