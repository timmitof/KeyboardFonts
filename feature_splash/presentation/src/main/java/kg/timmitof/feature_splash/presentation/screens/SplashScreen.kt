package kg.timmitof.feature_splash.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kg.timmitof.core.ui.base.Container
import kg.timmitof.feature_splash.presentation.components.DumpingSplash

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel()
) {
    Container(viewModel = viewModel) { state, _ ->
        // Со сплеша нельзя уйти — ждём окончания загрузки шаблонов
        onBack { }

        val onFinish = remember { { sendEvent(SplashEvent.AnimationFinished) } }

        DumpingSplash(
            isLoading = state.value.isLoading,
            onFinish = onFinish
        )
    }
}
