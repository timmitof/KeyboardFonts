package kg.timmitof.keyboard.presentation.components.translator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.presentation.components.keyboardClickable
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.screens.keyboard.rememberSlice
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.theme.KFTheme

/**
 * Перевод скопированного над клавишами (дизайн 8b): откуда скопировано, оригинал, перевод и два действия —
 * вставить перевод или ответить на языке собеседника (переводчик откроется с готовой парой).
 */
@Composable
internal fun ClipTranslationCard(
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clip by state.rememberSlice { it.clipTranslation.takeIf { _ -> it.translator == null } }
    val translation = clip ?: return
    val sourceName = translation.source.name

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 6.dp)
            .clip(CardShape)
            .background(KFTheme.color.keyButtonBackground)
            .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_translate),
                contentDescription = null,
                tint = KFTheme.color.keyAccentBackground,
                modifier = Modifier.size(15.dp),
            )
            Text(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f),
                text = stringResource(R.string.clip_translation_header, sourceName).uppercase(),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.7.sp,
                color = KFTheme.color.keySpecialTextColor,
                maxLines = 1,
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .keyboardClickable { onEvent(KeyboardEvent.OnClipTranslationDismiss) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.clip_translation_close),
                    tint = KFTheme.color.keySpecialTextColor,
                    modifier = Modifier.size(15.dp),
                )
            }
        }

        Text(
            modifier = Modifier.padding(end = 6.dp),
            text = translation.original,
            fontSize = 12.5.sp,
            color = KFTheme.color.keySpecialTextColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            modifier = Modifier.padding(end = 6.dp),
            text = translation.translation,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = KFTheme.color.keyLabelColor,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CardAction(
                label = stringResource(R.string.clip_translation_paste),
                background = KFTheme.color.keySpecialButtonBackground,
                content = KFTheme.color.keySpecialLabelColor,
                onClick = { onEvent(KeyboardEvent.OnClipTranslationPaste) },
            )
            CardAction(
                label = stringResource(R.string.clip_translation_reply, sourceName),
                background = KFTheme.color.keyAccentBackground,
                content = KFTheme.color.keyAccentTextColor,
                iconRes = R.drawable.ic_translate,
                onClick = { onEvent(KeyboardEvent.OnClipTranslationReply) },
            )
        }
    }
}

@Composable
private fun CardAction(
    label: String,
    background: Color,
    content: Color,
    onClick: () -> Unit,
    iconRes: Int? = null,
) {
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(background)
            .keyboardClickable(onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        iconRes?.let {
            Icon(
                imageVector = ImageVector.vectorResource(it),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(15.dp),
            )
        }
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = content,
            maxLines = 1,
        )
    }
}

private val CardShape = RoundedCornerShape(16.dp)
