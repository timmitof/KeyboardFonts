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
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch

@Composable
internal fun DumpingSplash() {
    val windowInfo = LocalWindowInfo.current
    val screenHeightPx = windowInfo.containerSize.height.toFloat()

    val imageOffsetY = remember { Animatable(-screenHeightPx / 2f) }
    val rotation = remember { Animatable(0f) }
    val logoSize = remember { Animatable(114f) }
    val clipShape = remember { Animatable(0f) }

    var showOnlyBackgroundLogo by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        imageOffsetY.animateTo(
            targetValue = 20f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow)
        )

        showOnlyBackgroundLogo = true
        launch {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = 400, easing = LinearEasing)
            )
        }
        launch {
            logoSize.animateTo(
                targetValue = 18f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
        launch {
            clipShape.animateTo(
                targetValue = 100f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }

        delay(500)
        showText = true
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = showText,
                enter = slideInHorizontally(
                    initialOffsetX = { it }
                ) + fadeIn(animationSpec = tween(400))
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
                painter = painterResource(
                    if (showOnlyBackgroundLogo) R.drawable.ic_logo_background else R.drawable.ic_logo
                ),
                contentDescription = null,
                modifier = Modifier
                    .rotate(rotation.value)
                    .size(logoSize.value.dp)
                    .graphicsLayer {
                        translationY = imageOffsetY.value
                    }
                    .clip(RoundedCornerShape(clipShape.value.toInt()))
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun Preview() {
    DumpingSplash()
}