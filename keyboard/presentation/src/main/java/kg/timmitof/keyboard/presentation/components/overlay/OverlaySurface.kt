package kg.timmitof.keyboard.presentation.components.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.consumeTouches
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.theme.KFTheme

/**
 * Подложка окна поверх клавиатуры.
 */
@Composable
internal fun BoxScope.OverlaySurface(
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .matchParentSize()
            .background(KFTheme.color.keyboardBackground)
            .consumeTouches()
            .padding(vertical = 8.dp),
        content = content
    )
}

/** Шапка окна: название слева, кнопки действий и крестик справа. */
@Composable
internal fun OverlayHeader(
    title: String,
    onClose: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeaderHeight)
            .background(KFTheme.color.keyboardBackground)
            .padding(start = 16.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = KFTheme.color.overlayTitleColor,
            maxLines = 1,
        )

        actions()

        OverlayIconButton(
            iconRes = R.drawable.ic_close,
            contentDescription = stringResource(R.string.overlay_close),
            onClick = onClose
        )
    }
}

/** Разделитель между переключателями и ярлыками. */
@Composable
internal fun OverlayDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 3.dp)
            .height(1.dp)
            .background(KFTheme.color.overlayDivider)
    )
}

private val HeaderHeight = 40.dp
