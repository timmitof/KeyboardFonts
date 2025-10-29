package kg.timmitof.feature_home.presentation.screens

import KeyboardFonts.feature_home.home.presentation.R
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
import kg.timmitof.feature_home.presentation.components.YourWorksCarousel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    Container(
        modifier = Modifier.fillMaxSize(),
        viewModel = viewModel,
        topBar = {
            HomeTopAppBar()
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
    state: HomeState,
    innerPadding: PaddingValues = PaddingValues()
) {
    val horizontalPadding = 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding())
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
                carouselList = listOf(1, 2, 3, 4, 5)
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
            LazyVerticalStaggeredGrid(
                modifier = Modifier.fillMaxSize(),
                columns = StaggeredGridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalItemSpacing = 14.dp,
                contentPadding = PaddingValues(
                    bottom = innerPadding.calculateBottomPadding() + horizontalPadding,
                    start = horizontalPadding,
                    end = horizontalPadding
                )
            ) {
                items(15) { BackgroundSurface(painter = painterResource(R.drawable.geometry_background)) }
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
                state = HomeState()
            )
        }
    }
}