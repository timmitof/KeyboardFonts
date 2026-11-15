package kg.timmitof.core.ui.components.brand

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.core.ui.theme.appColors

/** Смена роли перекрашивает плашку плавно: цвет читается в фазе отрисовки, без рекомпозиции. */
@Composable
fun AccentIconTile(
    icon: Painter,
    role: AccentRole,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = 22.dp,
    cornerRadius: Dp = 12.dp,
) {
    val tones = MaterialTheme.appColors[role]
    val container by animateColorAsState(tones.container, spring(stiffness = 500f), label = "tileContainer")
    val content by animateColorAsState(tones.solid, spring(stiffness = 500f), label = "tileContent")

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .drawBehind { drawRect(container) },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(iconSize),
        )
    }
}
