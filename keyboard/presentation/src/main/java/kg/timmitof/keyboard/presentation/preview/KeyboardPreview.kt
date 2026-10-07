package kg.timmitof.keyboard.presentation.preview

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.domain.model.KeyboardKey
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.font.domain.model.KeyboardFont
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.components.KeyCornerRadius
import kg.timmitof.keyboard.presentation.components.KeyRowHeight
import kg.timmitof.keyboard.presentation.components.KeyRowSpacing
import kg.timmitof.keyboard.presentation.components.KeySpacing
import kg.timmitof.keyboard.presentation.components.TopBarHeight
import kg.timmitof.keyboard.presentation.components.keys.keySurface
import kg.timmitof.keyboard.presentation.theme.KFTheme
import kg.timmitof.keyboard.presentation.theme.KeyboardTheme
import kg.timmitof.keyboard.presentation.theme.appearance
import kg.timmitof.keyboard.presentation.theme.keyboardBackground

/** Уменьшенная копия клавиатуры для приложения: те же цвета, раскладка и клавиши, но без ввода. */
@Composable
fun KeyboardPreview(
    layout: KeyboardLayout?,
    settings: KeyboardSettings,
    fonts: List<KeyboardFont>,
    selectedFont: KeyboardFont,
    languageName: String,
    modifier: Modifier = Modifier,
    shape: Shape = PreviewShape,
) {
    val isSystemDark = isSystemInDarkTheme()
    val appearance = remember(settings, isSystemDark) { settings.appearance(isSystemDark) }

    val rowHeight by animateDpAsState(
        targetValue = KeyRowHeight * settings.height.scale * PreviewScale,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f),
        label = "previewRowHeight",
    )

    val rows = remember(layout, settings.isDigitsRowEnabled) {
        val letters = layout ?: return@remember emptyList()
        (if (settings.isDigitsRowEnabled) letters.withDigitsRow() else letters).rows
    }

    KeyboardTheme(appearance = appearance) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(KFTheme.color.keyboardBackground)
                .keyboardBackground(settings.background, maxPhotoSide = PreviewPhotoSide)
                .padding(horizontal = 2.dp, vertical = 5.dp),
        ) {
            PreviewTopBar(
                fonts = fonts,
                selectedFont = selectedFont,
                isFontsVisible = settings.isFontsPanelEnabled,
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    row.forEach { key ->
                        PreviewKey(
                            key = key,
                            font = selectedFont,
                            languageName = languageName,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewTopBar(
    fonts: List<KeyboardFont>,
    selectedFont: KeyboardFont,
    isFontsVisible: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(TopBarHeight * PreviewScale)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            modifier = Modifier.weight(1f),
            visible = isFontsVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                fonts.take(PreviewFontsCount).forEach { font ->
                    PreviewFontChip(font = font, isSelected = font.id == selectedFont.id)
                }
            }
        }
        if (!isFontsVisible) Spacer(modifier = Modifier.weight(1f))

        PreviewAnchor(iconRes = R.drawable.ic_clipboard)
        PreviewAnchor(iconRes = R.drawable.ic_settings_gear)
    }
}

@Composable
private fun PreviewFontChip(font: KeyboardFont, isSelected: Boolean) {
    val text = remember(font) { font.apply(FontSample) }

    Box(
        modifier = Modifier
            .height(22.dp)
            .clip(CircleShape)
            .background(if (isSelected) KFTheme.color.keySpecialButtonBackground else Color.Transparent)
            .padding(horizontal = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) {
                KFTheme.color.keySpecialLabelColor
            } else {
                KFTheme.color.keyTextColor.copy(alpha = 0.55f)
            },
            maxLines = 1,
        )
    }
}

@Composable
private fun PreviewAnchor(@DrawableRes iconRes: Int) {
    Box(modifier = Modifier.size(26.dp), contentAlignment = Alignment.Center) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = KFTheme.color.keySpecialTextColor,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun RowScope.PreviewKey(
    key: KeyboardKey,
    font: KeyboardFont,
    languageName: String,
) {
    when (key) {
        is KeyboardKey.Spacer -> Spacer(modifier = Modifier.weight(key.weight))

        is KeyboardKey.Character -> {
            val label = remember(key, font) { font.apply(key.labelLower) }
            PreviewKeyCap(weight = key.weight, isSpecial = key.isSpecial) {
                PreviewLabel(text = label, fontSize = if (key.isSpecial) 10 else 15)
            }
        }

        is KeyboardKey.Space -> PreviewKeyCap(weight = key.weight, isSpecial = false) {
            PreviewLabel(text = languageName, fontSize = 9, isMuted = true)
        }

        is KeyboardKey.Shift -> PreviewIconKey(key.weight, R.drawable.ic_shift_key)
        is KeyboardKey.Backspace -> PreviewIconKey(key.weight, R.drawable.ic_backspace_key)
        is KeyboardKey.Enter -> PreviewIconKey(
            weight = key.weight,
            iconRes = R.drawable.ic_enter_key,
            background = KFTheme.color.keyEnterBackground,
            tint = KFTheme.color.keyEnterTextColor,
        )
        is KeyboardKey.HideKeyboard -> PreviewIconKey(key.weight, R.drawable.ic_keyboard_hide)
        is KeyboardKey.EmojiSwitch -> PreviewIconKey(key.weight, R.drawable.ic_emoji_key)

        is KeyboardKey.SymbolsSwitch -> PreviewTextKey(key.weight, SymbolsLabel)
        is KeyboardKey.SymbolsAltSwitch -> PreviewTextKey(key.weight, key.label)
        is KeyboardKey.AbcSwitch -> PreviewTextKey(key.weight, AbcLabel)
    }
}

@Composable
private fun RowScope.PreviewIconKey(
    weight: Float,
    @DrawableRes iconRes: Int,
    background: Color = KFTheme.color.keySpecialButtonBackground,
    tint: Color = KFTheme.color.keySpecialLabelColor,
) {
    PreviewKeyCap(weight = weight, isSpecial = true, background = background) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun RowScope.PreviewTextKey(weight: Float, label: String) {
    PreviewKeyCap(weight = weight, isSpecial = true) {
        PreviewLabel(text = label, fontSize = 10, isSpecial = true)
    }
}

@Composable
private fun PreviewLabel(
    text: String,
    fontSize: Int,
    isSpecial: Boolean = false,
    isMuted: Boolean = false,
) {
    Text(
        text = text,
        fontSize = fontSize.sp,
        fontWeight = if (isSpecial) FontWeight.Medium else FontWeight.Normal,
        color = when {
            isSpecial -> KFTheme.color.keySpecialLabelColor
            isMuted -> KFTheme.color.keyLabelColor.copy(alpha = 0.55f)
            else -> KFTheme.color.keyLabelColor
        },
        maxLines = 1,
    )
}

@Composable
private fun RowScope.PreviewKeyCap(
    weight: Float,
    isSpecial: Boolean,
    background: Color = if (isSpecial) {
        KFTheme.color.keySpecialButtonBackground
    } else {
        KFTheme.color.keyButtonBackground
    },
    content: @Composable () -> Unit,
) {
    val shadow = KFTheme.color.keyButtonShadow
    val outline = KFTheme.color.keyOutline.takeIf { KFTheme.isKeyOutlined }

    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .padding(horizontal = KeySpacing * PreviewScale / 2, vertical = KeyRowSpacing * PreviewScale / 2)
            .keySurface(
                surface = { background },
                support = { shadow },
                cornerRadius = PreviewKeyRadius,
                outline = outline,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

private const val PreviewScale = 0.66f

private const val PreviewFontsCount = 4

private const val FontSample = "Abc"
private const val SymbolsLabel = "123"
private const val AbcLabel = "ABC"

private val PreviewKeyRadius: Dp = KeyCornerRadius * PreviewScale
private val PreviewShape = RoundedCornerShape(14.dp)
private const val PreviewPhotoSide = 1080
