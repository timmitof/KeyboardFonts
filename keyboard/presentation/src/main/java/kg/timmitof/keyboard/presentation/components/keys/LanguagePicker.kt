package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.presentation.components.AboveAnchorPopupPositionProvider
import kg.timmitof.keyboard.presentation.theme.KFTheme
import kotlin.math.abs

/**
 * Попап выбора языка.
 *
 * @param languages доступные языки.
 * @param width ширина попапа (ширина клавиши пробела).
 * @param floatIndex плавающий индекс языка под пальцем (лямбда — чтобы лента
 *   двигалась в фазе рисования без рекомпозиций).
 * @param onDismiss запрос на закрытие попапа.
 */
@Composable
internal fun LanguagePicker(
    languages: List<KeyboardLanguage>,
    width: Dp,
    floatIndex: () -> Float,
    onDismiss: () -> Unit,
) {
    val marginPx = with(LocalDensity.current) { 8.dp.roundToPx() }
    val positionProvider = remember(marginPx) { AboveAnchorPopupPositionProvider(marginPx) }
    val shape = RoundedCornerShape(16.dp)

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = false, clippingEnabled = false)
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .clip(shape)
                .background(KFTheme.color.keyButtonPressedBackground, shape)
        ) {
            languages.forEachIndexed { index, language ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            val delta = index - floatIndex()
                            translationX = delta * size.width
                            alpha = 1f - 0.5f * abs(delta).coerceAtMost(1f)
                        }
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = language.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = KFTheme.color.keyTextColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
