package kg.timmitof.core.ui.components.brand

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.R
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.core.ui.theme.appColors

/**
 * Пилюля статуса в шапке: «Активна», «Подключена», «Не подключена».
 *
 * Роль задаёт цвет: [AccentRole.SUCCESS] рисуется с галочкой, остальные — с точкой.
 * Смена статуса перекрашивает пилюлю плавно, без перестройки разметки.
 *
 * @param onClick `null` — пилюля только показывает статус.
 */
@Composable
fun StatusPill(
    text: String,
    role: AccentRole,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val tones = MaterialTheme.appColors[role]
    val container by animateColorAsState(tones.container, spring(stiffness = 500f), label = "pill")
    val content by animateColorAsState(tones.onContainer, spring(stiffness = 500f), label = "pillText")

    Row(
        modifier = modifier
            .height(28.dp)
            .clip(CircleShape)
            .drawBehind { drawRect(container) }
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = 9.dp, end = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (role == AccentRole.SUCCESS) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(13.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(content)
            )
        }
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = content,
            maxLines = 1,
        )
    }
}
