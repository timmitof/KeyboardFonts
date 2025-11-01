package kg.timmitof.feature_splash.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kg.timmitof.core.common.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
internal fun DumpingSplash(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true,
    onFinish: () -> Unit= {}
) {
    val windowInfo = LocalWindowInfo.current
    val screenHeightPx = windowInfo.containerSize.height.toFloat()

    val offsetY = remember { Animatable(-screenHeightPx / 2f) }
    val rotation = remember { Animatable(0f) }
    val logoSize = remember { Animatable(114f) }
    val clipRadius = remember { Animatable(0f) }

    var showOnlyBgLogo by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }

    val logo = painterResource(R.drawable.ic_logo)
    val logoBg = painterResource(R.drawable.ic_logo_background)

    val currentIsLoading = rememberUpdatedState(isLoading)

    LaunchedEffect(Unit) {
        offsetY.animateTo(10f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow))

        showOnlyBgLogo = true

        launch { rotation.animateTo(360f, tween(400, easing = LinearEasing)) }
        launch { logoSize.animateTo(18f, tween(400, easing = FastOutSlowInEasing)) }
        launch { clipRadius.animateTo(100f, tween(400, easing = FastOutSlowInEasing)) }

        delay(500)
        showText = true

        delay(1000)

        if (currentIsLoading.value) {
            snapshotFlow { currentIsLoading.value }
                .filter { !it }
                .first()
        }
        onFinish()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = showText,
                enter = slideInHorizontally { it } + fadeIn(tween(400))
            ) {
                Text(
                    modifier = Modifier.padding(end = 8.dp),
                    text = stringResource(KeyboardFonts.feature_splash.splash.presentation.R.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = FontFamily(Font(R.font.sf_pro_rounded_black)),
                    fontWeight = FontWeight.Bold
                )
            }

            Image(
                painter = if (showOnlyBgLogo) logoBg else logo,
                contentDescription = null,
                modifier = Modifier
                    .size(logoSize.value.dp)
                    .rotate(rotation.value)
                    .graphicsLayer { translationY = offsetY.value }
                    .clip(RoundedCornerShape(clipRadius.value.toInt()))
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun Preview() {
    DumpingSplash()
}