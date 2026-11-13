package kg.timmitof.feature_settings.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.R as UiR
import kg.timmitof.core.ui.components.settings.SettingsBadge
import kg.timmitof.core.ui.theme.appColors

/** Строка языка в списках настроек: код в плашке, название и подпись под ним. Общая для раскладок и переводчика. */
@Composable
internal fun RowScope.LanguageRowContent(
    badge: String,
    title: String,
    description: String,
) {
    SettingsBadge(text = badge)
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
        )
        Text(
            text = description,
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
        )
    }
}

/** Круглая кнопка-иконка в конце строки: убрать язык, скачать язык. */
@Composable
internal fun RowIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.outline,
) {
    Box(
        modifier = Modifier
            .size(RowTrailingSize)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** Готово: язык подключён или скачан. */
@Composable
internal fun ConnectedMark() {
    val tones = MaterialTheme.appColors.success

    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(tones.container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(UiR.drawable.ic_check),
            contentDescription = null,
            tint = tones.solid,
            modifier = Modifier.size(13.dp),
        )
    }
}

/** Идёт загрузка: круглый лоадер на месте кнопки, строка не прыгает. */
@Composable
internal fun RowProgress() {
    Box(modifier = Modifier.size(RowTrailingSize), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private val RowTrailingSize = 32.dp
