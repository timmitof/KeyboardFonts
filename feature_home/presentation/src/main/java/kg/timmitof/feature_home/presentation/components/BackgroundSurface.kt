package kg.timmitof.feature_home.presentation.components

import kg.timmitof.feature_home.presentation.R
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade

private const val A4_ASPECT_RATIO = 210f / 297f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ScalableSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    onClick: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
    )

    Surface(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .aspectRatio(A4_ASPECT_RATIO),
        shape = shape,
        color = backgroundColor,
        interactionSource = interactionSource,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize(), content = content)
    }
}

@Composable
internal fun BackgroundSurface(
    modifier: Modifier = Modifier,
    backgroundFilePath: String,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    onClick: () -> Unit = {}
) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(backgroundFilePath)
            .crossfade(true)
            .build()
    )

    ScalableSurface(
        modifier = modifier,
        shape = shape,
        backgroundColor = MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
internal fun AddImageCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    onClick: () -> Unit = {}
) {
    ScalableSurface(
        modifier = modifier,
        shape = shape,
        backgroundColor = MaterialTheme.colorScheme.surfaceDim,
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier
                .align(Alignment.Center)
                .size(56.dp),
            painter = painterResource(R.drawable.ic_plus),
            contentDescription = stringResource(R.string.create_background),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
private fun Preview() {
    ScalableSurface {  }
}