package kg.timmitof.feature_home.presentation.screens

import kg.timmitof.feature_home.presentation.R
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kg.timmitof.core.ui.base.Container
import kg.timmitof.core.ui.base.ContainerDSLBuilder
import kg.timmitof.core.ui.theme.KeyboardFontsTheme
import kg.timmitof.feature_home.presentation.components.AnimatedSection
import kg.timmitof.feature_home.presentation.components.BackgroundSurface
import kg.timmitof.feature_home.presentation.components.HomeTopAppBar
import kg.timmitof.feature_home.presentation.components.TwoColumnGrid
import kg.timmitof.feature_home.presentation.components.YourWorksCarousel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Container(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        viewModel = viewModel,
        topBar = {
            HomeTopAppBar(scrollBehavior = scrollBehavior)
        }
    ) { state, innerPadding ->
        HomeContent(
            state = state,
            innerPadding = innerPadding
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ContainerDSLBuilder<HomeSideEffect, HomeEvent>.HomeContent(
    state: State<HomeState>,
    innerPadding: PaddingValues = PaddingValues()
) {
    val horizontalPadding = 16.dp
    val scrollState = rememberScrollState()

    val templateList = remember(state.value.templateList) { state.value.templateList }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding())
            .verticalScroll(scrollState)
    ) {
        AnimatedSection(
            title = stringResource(R.string.your_works),
            enterAnimation = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()
            ),
            horizontalPadding = horizontalPadding
        ) {
            YourWorksCarousel(
                carouselList = templateList
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedSection(
            title = stringResource(R.string.backgrounds),
            enterAnimation = slideInVertically(
                initialOffsetY = { it },
                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()
            ),
            horizontalPadding = horizontalPadding
        ) {
            TwoColumnGrid(
                modifier = Modifier.padding(horizontal = horizontalPadding),
                items = templateList
            ) {
                BackgroundSurface(
                    backgroundFilePath = it.bitmapFilePath,
                    onClick = { sendEvent(HomeEvent.BackgroundSelected(it.bitmapFilePath)) }
                )
            }
        }
    }
}

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