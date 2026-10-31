package kg.timmitof.feature_splash.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kg.timmitof.core.ui.base.Container
import kg.timmitof.feature_splash.presentation.components.DumpingSplash
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = koinViewModel()
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
