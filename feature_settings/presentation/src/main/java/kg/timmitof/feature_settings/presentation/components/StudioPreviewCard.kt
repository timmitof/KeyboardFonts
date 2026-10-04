package kg.timmitof.feature_settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.theme.appColors
import kg.timmitof.feature_settings.domain.model.SettingsSummary
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.presentation.preview.KeyboardPreview

@Composable
internal fun StudioPreviewCard(
    settings: KeyboardSettings,
    summary: SettingsSummary,
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
            settings = settings,
            fonts = summary.fonts,
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
    var text by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    val textStyle = TextStyle(
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onBackground,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(FieldShape)
            .background(MaterialTheme.appColors.cardMuted)
            .padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                textStyle = textStyle,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
            if (text.isEmpty()) {
                Text(text = placeholder, style = textStyle, maxLines = 1)
            }
        }
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
    }
}

private const val SampleEmoji = "👋"

private val CardShape = RoundedCornerShape(22.dp)
private val FieldShape = RoundedCornerShape(12.dp)
