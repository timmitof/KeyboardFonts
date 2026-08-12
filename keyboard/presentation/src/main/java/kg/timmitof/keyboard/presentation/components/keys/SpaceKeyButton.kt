package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.spaceCursorClickable
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.presentation.theme.KFTheme
import kotlin.math.roundToInt

/** Смещение пальца, за которое слайд по пробелу переключает один язык. */
private val LanguageSlideStep = 80.dp

@Composable
internal fun RowScope.SpaceKeyButton(
    modifier: Modifier = Modifier,
    weight: Float,
    languages: List<KeyboardLanguage>,
    selectedLanguage: KeyboardLanguage? = null,
    isLanguageSlideEnabled: Boolean = true,
    onLanguageSelect: (KeyboardLanguage) -> Unit = {},
    onCursorMove: (horizontal: Int, vertical: Int) -> Unit = { _, _ -> },
    onCursorModeChange: (active: Boolean) -> Unit = {},
    onClick: () -> Unit
) {
    val slideStepPx = with(LocalDensity.current) { LanguageSlideStep.toPx() }
    var isPickerVisible by remember { mutableStateOf(false) }
    var slideOffsetPx by remember { mutableFloatStateOf(0f) }
    var keyWidthPx by remember { mutableIntStateOf(0) }

    val pickerLanguages = remember(languages, selectedLanguage) {
        val others = languages.filter { it != selectedLanguage }
        if (selectedLanguage == null || others.isEmpty()) languages
        else others + selectedLanguage + others
    }
    val centerIndex = pickerLanguages.indexOf(selectedLanguage).coerceAtLeast(0)

    val floatIndexFor: (Float) -> Float = { offsetPx ->
        (centerIndex - offsetPx / slideStepPx)
            .coerceIn(0f, pickerLanguages.lastIndex.coerceAtLeast(0).toFloat())
    }

    KeyBase(
        modifier = modifier
            .weight(weight)
            .fillMaxHeight()
            .onSizeChanged { keyWidthPx = it.width },
        background = KFTheme.color.keyButtonBackground,
        shadowColor = KFTheme.color.keyButtonShadow,
        customGestures = { interactionSource ->
            Modifier.spaceCursorClickable(
                interactionSource = interactionSource,
                onTap = onClick,
                onSlideStart = {
                    slideOffsetPx = 0f
                    isPickerVisible = isLanguageSlideEnabled && languages.size > 1
                },
                onSlideChange = { slideOffsetPx = it },
                onSlideFinish = { offsetPx ->
                    if (!isPickerVisible) return@spaceCursorClickable
                    isPickerVisible = false
                    pickerLanguages.getOrNull(floatIndexFor(offsetPx).roundToInt())
                        ?.takeIf { it != selectedLanguage }
                        ?.let(onLanguageSelect)
                },
                onCursorStart = { onCursorModeChange(true) },
                onCursorMove = onCursorMove,
                onCursorEnd = { onCursorModeChange(false) }
            )
        },
        onClick = onClick
    ) {
        LanguageLabel(
            selectedLanguage = selectedLanguage,
            showArrows = isLanguageSlideEnabled && languages.size > 1
        )

        if (isPickerVisible) {
            LanguagePicker(
                languages = pickerLanguages,
                width = with(LocalDensity.current) { keyWidthPx.toDp() },
                floatIndex = { floatIndexFor(slideOffsetPx) },
                onDismiss = { isPickerVisible = false }
            )
        }
    }
}

@Composable
private fun LanguageLabel(
    modifier: Modifier = Modifier,
    selectedLanguage: KeyboardLanguage? = null,
    showArrows: Boolean = true
) {
    if (selectedLanguage == null) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalArrangement = if (showArrows) Arrangement.SpaceBetween else Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showArrows) LanguageArrows("‹")

        Text(
            text = selectedLanguage.displayName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = KFTheme.color.keyTextColor,
            letterSpacing = 0.5.sp
        )

        if (showArrows) LanguageArrows("›")
    }
}

@Composable
private fun LanguageArrows(label: String) {
    Text(
        text = label,
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium,
        color = KFTheme.color.keyTextColor.copy(alpha = 0.5f),
        letterSpacing = 0.sp
    )
}
