package kg.timmitof.feature_settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.components.field.ProbeTextField
import kg.timmitof.core.ui.theme.appColors
import kg.timmitof.feature_settings.domain.model.SettingsSummary
import kg.timmitof.keyboard.domain.model.KeyboardBackground
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.font.domain.model.KeyboardFont
import kg.timmitof.keyboard.presentation.preview.KeyboardPreview

@Composable
internal fun StudioPreviewCard(
    settings: KeyboardSettings,
    draft: () -> KeyboardBackground.Solid?,
    summary: SettingsSummary,
    fonts: List<KeyboardFont>,
    sample: String,
    checkLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.appColors.card)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TryField(
            placeholder = remember(summary.selectedFont, sample) {
                "${summary.selectedFont.apply(sample)} $SampleEmoji"
            },
            checkLabel = checkLabel,
        )

        KeyboardPreview(
            layout = summary.previewLayout,
            settings = draft()?.let { settings.copy(background = it) } ?: settings,
            fonts = fonts,
            selectedFont = summary.selectedFont,
            languageName = summary.selectedLanguage?.displayName.orEmpty(),
        )
    }
}

@Composable
private fun TryField(
    placeholder: String,
    checkLabel: String,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    ProbeTextField(
        placeholder = placeholder,
        containerColor = MaterialTheme.appColors.cardMuted,
        cursorColor = MaterialTheme.colorScheme.primary,
        textStyle = TextStyle(fontSize = 15.sp),
        shape = FieldShape,
        contentPadding = PaddingValues(start = 12.dp),
        singleLine = true,
        focusRequester = focusRequester,
        modifier = Modifier.height(40.dp),
        trailing = {
            Text(
                modifier = Modifier
                    .clip(FieldShape)
                    .clickable {
                        focusRequester.requestFocus()
                        keyboard?.show()
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                text = checkLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
        },
    )
}

private const val SampleEmoji = "👋"

private val CardShape = RoundedCornerShape(22.dp)
private val FieldShape = RoundedCornerShape(12.dp)
