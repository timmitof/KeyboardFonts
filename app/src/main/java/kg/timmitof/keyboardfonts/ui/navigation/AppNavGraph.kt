package kg.timmitof.keyboardfonts.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import kg.timmitof.core.analytics.Analytics
import kg.timmitof.core.analytics.AnalyticsEvents
import kg.timmitof.core.navigation.graphs.SplashGraph
import kg.timmitof.feature_home.presentation.navigation.homeGraph
import kg.timmitof.feature_settings.presentation.navigation.settingsGraph
import kg.timmitof.feature_splash.presentation.navigation.splashGraph

@Composable
internal fun AppNavHost(navController: NavHostController, analytics: Analytics) {
    ScreenViewTracking(navController, analytics)

    NavHost(navController = navController, startDestination = SplashGraph) {
        settingsGraph()
        homeGraph()
        splashGraph()
    }
}

/** Активность одна, поэтому экраны Firebase сам не различает — отправляем screen_view по смене destination. */
@Composable
private fun ScreenViewTracking(navController: NavController, analytics: Analytics) {
    DisposableEffect(navController, analytics) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            destination.route?.let { analytics.log(AnalyticsEvents.screenView(it.screenName())) }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }
}

/** Маршрут type-safe навигации — полное имя класса с аргументами: оставляем только имя экрана. */
private fun String.screenName(): String = substringBefore('/').substringBefore('?').substringAfterLast('.')
