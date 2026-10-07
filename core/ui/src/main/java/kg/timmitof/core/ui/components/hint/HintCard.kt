package kg.timmitof.core.ui.components.hint

import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.R
import kg.timmitof.core.ui.theme.appColors
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun HintCard(
    title: String,
    body: String,
    icon: Painter,
    modifier: Modifier = Modifier,
) {
    val tones = MaterialTheme.appColors.hint

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(HintShape)
            .background(tones.container)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = tones.onContainer,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(19.dp),
        )
        Column {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = tones.onContainer,
            )
            Text(
                modifier = Modifier.padding(top = 2.dp),
                text = body,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = tones.onContainer,
            )
        }
    }
}

@Composable
fun TipsCard(
    tips: List<String>,
    counter: (index: Int, total: Int) -> String,
    modifier: Modifier = Modifier,
) {
    if (tips.isEmpty()) return

    val tones = MaterialTheme.appColors.hint
    val pagerState = rememberPagerState(pageCount = { tips.size })
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(TipsShape)
            .background(tones.container)
            .clickable {
                val next = (pagerState.currentPage + 1) % tips.size
                scope.launch { pagerState.animateScrollToPage(next, animationSpec = TipSpring) }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_lightbulb),
            contentDescription = null,
            tint = tones.onContainer,
            modifier = Modifier
                .padding(start = 14.dp)
                .size(20.dp),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            // Невидимая подложка из всех советов задаёт высоту по самому длинному: плашка не прыгает, пейджер получает конечную высоту.
            tips.forEachIndexed { index, tip ->
                TipText(
                    counter = counter(index + 1, tips.size),
                    tip = tip,
                    color = tones.onContainer,
                    modifier = Modifier
                        .graphicsLayer { alpha = 0f }
                        .clearAndSetSemantics { },
                )
            }

            // Когда советы кончаются, вертикальный свайп сам уходит в прокрутку экрана.
            VerticalPager(
                state = pagerState,
                modifier = Modifier.matchParentSize(),
                beyondViewportPageCount = 1,
            ) { page ->
                TipText(
                    counter = counter(page + 1, tips.size),
                    tip = tips[page],
                    color = tones.onContainer,
                    modifier = Modifier.graphicsLayer {
                        alpha = 1f - pagerState.offsetFor(page).coerceIn(0f, 1f)
                    },
                )
            }
        }

        TipDots(
            modifier = Modifier.padding(end = 14.dp),
            total = tips.size,
            pagerState = pagerState,
            color = tones.onContainer,
        )
    }
}

/** Один совет: счётчик над текстом. Отступы внутри — текст листается во всю высоту плашки. */
@Composable
private fun TipText(
    counter: String,
    tip: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = counter.uppercase(),
            fontSize = 11.sp,
            letterSpacing = 0.44.sp,
            fontWeight = FontWeight.Medium,
            color = color.copy(alpha = 0.75f),
        )
        Text(
            modifier = Modifier.padding(top = 3.dp),
            text = tip,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = color,
        )
    }
}

/** Яркость считается в draw-фазе из смещения страницы, поэтому свайп не вызывает рекомпозиций. */
@Composable
private fun TipDots(
    total: Int,
    pagerState: PagerState,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(total) { dot ->
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .graphicsLayer {
                        val closeness = 1f - pagerState.offsetFor(dot).coerceIn(0f, 1f)
                        alpha = DotIdleAlpha + (1f - DotIdleAlpha) * closeness
                    }
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

private fun PagerState.offsetFor(page: Int): Float =
    abs((currentPage - page) + currentPageOffsetFraction)

private const val DotIdleAlpha = 0.3f
private val TipSpring = spring<Float>(dampingRatio = 0.9f, stiffness = 400f)

private val HintShape = RoundedCornerShape(16.dp)
private val TipsShape = RoundedCornerShape(18.dp)
