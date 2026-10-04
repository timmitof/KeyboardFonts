package kg.timmitof.feature_settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.core.ui.theme.appColors

@Immutable
data class BackgroundAction(
    val label: String,
    val icon: Painter,
    val role: AccentRole,
    val onClick: () -> Unit,
)

/** Карточки делят ширину поровну: новая (например, «Нарисовать») — просто ещё одна запись в списке. */
@Composable
internal fun BackgroundActions(
    actions: List<BackgroundAction>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        actions.forEach { action ->
            BackgroundActionCard(action = action, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun BackgroundActionCard(action: BackgroundAction, modifier: Modifier = Modifier) {
    val tones = MaterialTheme.appColors[action.role]

    Column(
        modifier = modifier
            .height(78.dp)
            .clip(CardShape)
            .background(tones.container)
            .clickable(onClick = action.onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(tones.solid),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = action.icon,
                contentDescription = null,
                tint = tones.onSolid,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = action.label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = tones.onContainer,
            maxLines = 1,
        )
    }
}

private val CardShape = RoundedCornerShape(18.dp)
