package kg.timmitof.feature_home.presentation.components

import KeyboardFonts.feature_home.home.presentation.R
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourWorksCarousel(
    modifier: Modifier = Modifier,
    carouselList: List<Int>,
    onAddClick: () -> Unit = {},
    onBackgroundSelected: (Int) -> Unit = {}
) {
    val carouselItems = listOf<Int?>(null) + carouselList

    HorizontalMultiBrowseCarousel(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .graphicsLayer(clip = false),
        state = rememberCarouselState { carouselItems.size },
        preferredItemWidth = 190.dp,
        itemSpacing = 10.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { index ->
        val item = carouselItems[index]
        val shape = rememberMaskShape(MaterialTheme.shapes.extraLarge)

        if (item == null) {
            AddImageCard(
                modifier = Modifier
                    .maskClip(MaterialTheme.shapes.extraLarge),
                onClick = onAddClick
            )
        } else {
            BackgroundSurface(
                modifier = Modifier
                    .maskClip(MaterialTheme.shapes.extraLarge),
                painter = painterResource(R.drawable.geometry_background),
                shape = shape,
                onClick = { onBackgroundSelected(item) }
            )
        }
    }
}