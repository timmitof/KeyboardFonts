package kg.timmitof.feature_home.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import kg.timmitof.core.ui.base.Container
import kg.timmitof.core.ui.base.ContainerDSLBuilder
import kg.timmitof.core.ui.theme.KeyboardFontsTheme
import kg.timmitof.feature_home.domain.model.KeyboardSetupStep
import kg.timmitof.feature_home.presentation.components.HomeTopAppBar
import kg.timmitof.feature_home.presentation.components.KeyboardSetupGuide
import kg.timmitof.feature_home.presentation.components.TryKeyboardField
import org.orbitmvi.orbit.compose.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val screenState by viewModel.collectAsState()

    Container(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        viewModel = viewModel,
        topBar = {
            HomeTopAppBar(
                scrollBehavior = scrollBehavior,
                isKeyboardReady = screenState.keyboardSetup.currentStep == KeyboardSetupStep.DONE,
                onOpenKeyboardSettings = {
                    viewModel.onEvent(HomeEvent.SetupStepClicked(KeyboardSetupStep.ENABLE))
                }
            )
        }
    ) { state, innerPadding ->
        HomeContent(
            state = state,
            innerPadding = innerPadding
        )
    }
}

@Composable
internal fun ContainerDSLBuilder<HomeSideEffect, HomeEvent>.HomeContent(
    state: State<HomeState>,
    innerPadding: PaddingValues = PaddingValues()
) {
    val scrollState = rememberScrollState()
    val setup = state.value.keyboardSetup

    // Статус клавиатуры меняется в системных настройках — перечитываем его при каждом возврате
    LifecycleResumeEffect(Unit) {
        sendEvent(HomeEvent.KeyboardSetupChecked)
        onPauseOrDispose { }
    }

    val onStepClick = remember<(KeyboardSetupStep) -> Unit> {
        { step -> sendEvent(HomeEvent.SetupStepClicked(step)) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
            .padding(horizontal = HORIZONTAL_PADDING, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        KeyboardSetupGuide(
            setup = setup,
            onStepClick = onStepClick
        )

        TryKeyboardField()
    }
}

private val HORIZONTAL_PADDING = 16.dp

@Preview
@Composable
private fun Preview() {
    KeyboardFontsTheme {
        Surface {
            ContainerDSLBuilder<HomeSideEffect, HomeEvent>({}).HomeContent(
                state = remember { mutableStateOf(HomeState()) }
            )
        }
    }
}