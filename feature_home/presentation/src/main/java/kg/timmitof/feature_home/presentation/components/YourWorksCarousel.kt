package kg.timmitof.feature_home.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kg.timmitof.core.domain.model.TemplateModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourWorksCarousel(
    modifier: Modifier = Modifier,
    carouselList: List<TemplateModel>,
    onAddClick: () -> Unit = {},
    onBackgroundSelected: (TemplateModel) -> Unit = {}
) {
    val carouselItems = listOf<TemplateModel?>(null) + carouselList

    HorizontalMultiBrowseCarousel(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        state = rememberCarouselState { carouselItems.size },
        preferredItemWidth = 190.dp,
        itemSpacing = 10.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { index ->
        val item = carouselItems[index]
        val shape = rememberMaskShape(MaterialTheme.shapes.extraLarge)

        if (item == null) {
            AddImageCard(
                modifier = Modifier.maskClip(MaterialTheme.shapes.extraLarge),
                shape = shape,
                onClick = onAddClick
            )
        } else {
            BackgroundSurface(
                modifier = Modifier.maskClip(MaterialTheme.shapes.extraLarge),
                backgroundFilePath = item.filePath,
                shape = shape,
                onClick = { onBackgroundSelected(item) }
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    YourWorksCarousel(
        carouselList = emptyList()
    )
}