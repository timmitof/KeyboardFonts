package kg.timmitof.feature_settings.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.R as UiR
import kg.timmitof.core.ui.theme.appColors
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.presentation.theme.KeyboardColorScheme
import kg.timmitof.keyboard.presentation.theme.KeyboardDarkColor
import kg.timmitof.keyboard.presentation.theme.KeyboardLightColor

/**
 * Плитка темы: миниатюра клавиатуры и подпись.
 *
 * @property mode тема, которую выбирает плитка.
 * @property label подпись под миниатюрой.
 */
@Immutable
data class ThemeTile(
    val mode: KeyboardThemeMode,
    val label: String,
)

/**
 * Выбор темы плитками: каждая — миниатюра клавиатуры в цветах этой темы.
 *
 * Миниатюра рисуется цветами живой клавиатуры, поэтому плитка не расходится
 * с тем, что пользователь увидит при наборе. «Как в системе» — половина светлой,
 * половина тёмной.
 */
@Composable
internal fun ThemeTiles(
    tiles: List<ThemeTile>,
    selected: KeyboardThemeMode,
    onSelect: (KeyboardThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tiles.forEach { tile ->
            ThemeTileItem(
                modifier = Modifier.weight(1f),
                tile = tile,
                isSelected = tile.mode == selected,
                onClick = { onSelect(tile.mode) },
            )
        }
    }
}

@Composable
private fun ThemeTileItem(
    tile: ThemeTile,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tones = MaterialTheme.appColors.appearance

    val background by animateColorAsState(
        targetValue = if (isSelected) tones.container else MaterialTheme.appColors.card,
        animationSpec = spring(stiffness = 600f),
        label = "themeTile",
    )
    val checkScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 700f),
        label = "themeCheck",
    )

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(TileShape)
                .drawBehind { drawRect(background) }
                .clickable(onClick = onClick)
                .padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            KeyboardThumbnail(
                mode = tile.mode,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ThumbnailRatio),
            )
            Text(
                text = tile.label,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) tones.onContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }

        // Галочка выбранной темы выпрыгивает пружиной — масштаб в graphicsLayer, без рекомпозиций.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
                .size(20.dp)
                .graphicsLayer {
                    scaleX = checkScale
                    scaleY = checkScale
                }
                .clip(CircleShape)
                .background(tones.solid),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(UiR.drawable.ic_check),
                contentDescription = null,
                tint = tones.onSolid,
                modifier = Modifier.size(11.dp),
            )
        }
    }
}

/** Миниатюра клавиатуры: три ряда букв и нижний ряд с пробелом и Enter. */
@Composable
private fun KeyboardThumbnail(mode: KeyboardThemeMode, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.clip(ThumbnailShape)) {
        when (mode) {
            KeyboardThemeMode.LIGHT -> drawThumbnail(LightColors)
            KeyboardThemeMode.DARK -> drawThumbnail(DarkColors)
            KeyboardThemeMode.AUTO -> {
                clipRect(right = size.width / 2) { drawThumbnail(LightColors) }
                clipRect(left = size.width / 2) { drawThumbnail(DarkColors) }
            }
        }
    }
}

private fun DrawScope.drawThumbnail(colors: KeyboardColorScheme) {
    drawRect(colors.keyboardBackground)

    val padding = 5.dp.toPx()
    val gap = 2.dp.toPx()
    val rows = ThumbnailRows.size
    val keyHeight = (size.height - padding * 2 - gap * (rows - 1)) / rows
    val column = (size.width - padding * 2 - gap * (ThumbnailColumns - 1)) / ThumbnailColumns
    val radius = CornerRadius(2.dp.toPx())

    ThumbnailRows.forEachIndexed { rowIndex, row ->
        var x = padding
        val y = padding + rowIndex * (keyHeight + gap)
        row.forEach { key ->
            val width = column * key.span + gap * (key.span - 1)
            drawRoundRect(
                color = when (key.kind) {
                    ThumbKey.Kind.LETTER -> colors.keyButtonBackground
                    ThumbKey.Kind.SPECIAL -> colors.keySpecialButtonBackground
                    ThumbKey.Kind.ACCENT -> colors.keyAccentBackground
                },
                topLeft = Offset(x, y),
                size = Size(width, keyHeight),
                cornerRadius = radius,
            )
            x += width + gap
        }
    }
}

/** Клавиша миниатюры: сколько колонок занимает и как окрашена. */
private class ThumbKey(val span: Int, val kind: Kind) {
    enum class Kind { LETTER, SPECIAL, ACCENT }
}

private const val ThumbnailColumns = 10
private const val ThumbnailRatio = 1.45f

private val ThumbnailRows: List<List<ThumbKey>> = listOf(
    List(10) { ThumbKey(1, ThumbKey.Kind.LETTER) },
    List(10) { ThumbKey(1, ThumbKey.Kind.LETTER) },
    listOf(ThumbKey(1, ThumbKey.Kind.SPECIAL)) +
        List(8) { ThumbKey(1, ThumbKey.Kind.LETTER) } +
        ThumbKey(1, ThumbKey.Kind.SPECIAL),
    listOf(
        ThumbKey(2, ThumbKey.Kind.SPECIAL),
        ThumbKey(6, ThumbKey.Kind.LETTER),
        ThumbKey(2, ThumbKey.Kind.ACCENT),
    ),
)

private val LightColors: KeyboardColorScheme = KeyboardLightColor()
private val DarkColors: KeyboardColorScheme = KeyboardDarkColor()

private val TileShape = RoundedCornerShape(14.dp)
private val ThumbnailShape = RoundedCornerShape(10.dp)
