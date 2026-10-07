package kg.timmitof.feature_settings.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.R as UiR
import kg.timmitof.feature_settings.presentation.R
import kg.timmitof.keyboard.domain.model.KeyboardBackground
import kg.timmitof.keyboard.presentation.theme.KeyboardLightColor
import kg.timmitof.keyboard.presentation.theme.keyboardBackground

/** [background] = `null` — плитка «Добавить»: открывает выбор фото. [label] = `null` — без подписи. */
@Immutable
data class BackgroundTile(
    val label: String?,
    val background: KeyboardBackground?,
)

@Composable
internal fun BackgroundTiles(
    tiles: List<BackgroundTile>,
    selected: KeyboardBackground,
    onClick: (BackgroundTile) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val rows = remember(tiles) { tiles.chunked(Columns) }
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { tile ->
                    BackgroundTileItem(
                        tile = tile,
                        isSelected = tile.background?.isSameAs(selected) == true,
                        onClick = { onClick(tile) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(Columns - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun BackgroundTileItem(
    tile: BackgroundTile,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selection by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 700f),
        label = "backgroundTile",
    )
    val ringColor = MaterialTheme.colorScheme.onBackground
    val edgeColor = ringColor.copy(alpha = EdgeAlpha)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TileHeight)
                    // Кольцо выбора рисуется снаружи плитки в draw-фазе: без рекомпозиций и сдвигов.
                    .drawWithContent {
                        drawContent()
                        if (selection > 0f) {
                            val width = RingWidth.toPx() * selection
                            drawRoundRect(
                                color = ringColor,
                                topLeft = Offset(-width / 2, -width / 2),
                                size = Size(size.width + width, size.height + width),
                                cornerRadius = CornerRadius(TileRadius.toPx() + width / 2),
                                style = Stroke(width),
                            )
                        }
                    }
                    .clip(TileShape)
                    .tileFill(tile.background)
                    .drawBehind {
                        drawRoundRect(edgeColor, cornerRadius = CornerRadius(TileRadius.toPx()), style = Stroke(1.dp.toPx()))
                    }
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                if (tile.background == null) {
                    Icon(
                        painter = painterResource(UiR.drawable.ic_plus),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // У выбранного фото вместо галочки карандаш: повторное касание открывает настройку положения.
            SelectionBadge(
                iconRes = if (tile.background is KeyboardBackground.Photo) R.drawable.ic_edit else UiR.drawable.ic_check,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 5.dp, y = (-5).dp)
                    .graphicsLayer {
                        scaleX = selection
                        scaleY = selection
                    },
            )
        }

        tile.label?.let { label ->
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun Modifier.tileFill(fill: KeyboardBackground?): Modifier = when (fill) {
    null -> background(MaterialTheme.colorScheme.surfaceVariant)
    KeyboardBackground.None -> background(ThemeBackground)
    else -> keyboardBackground(fill, maxPhotoSide = TilePhotoSide)
}

/** Фото сравниваем по id: кадр мог поменяться, а фото то же. */
private fun KeyboardBackground.isSameAs(other: KeyboardBackground): Boolean =
    if (this is KeyboardBackground.Photo && other is KeyboardBackground.Photo) photo.id == other.photo.id else this == other

@Composable
private fun SelectionBadge(@DrawableRes iconRes: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onBackground),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.background,
            modifier = Modifier.size(11.dp),
        )
    }
}

private const val Columns = 4
private const val TilePhotoSide = 384
private const val EdgeAlpha = 0.06f

private val TileHeight = 50.dp
private val TileRadius = 12.dp
private val TileShape = RoundedCornerShape(TileRadius)
private val RingWidth = 2.5.dp
private val ThemeBackground: Color = KeyboardLightColor().keyboardBackground
