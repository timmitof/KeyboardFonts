package kg.timmitof.feature_splash.presentation.screens

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kg.timmitof.core.navigation.LocalNavController
import kg.timmitof.core.navigation.graphs.HomeGraph
import kg.timmitof.feature_splash.presentation.components.DumpingSplash
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val state = viewModel.collectAsState()

    DumpingSplash(
        isLoading = state.value.isLoading,
        onFinish = { navController.navigate(HomeGraph) }
    )
}