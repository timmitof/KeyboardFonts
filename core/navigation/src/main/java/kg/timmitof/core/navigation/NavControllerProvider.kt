package kg.timmitof.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController not found!")
}

@Composable
fun NavControllerProvider(
    content: @Composable () -> Unit
) {
    val navController = rememberNavController()
    CompositionLocalProvider(LocalNavController provides navController) {
        content()
    }
}