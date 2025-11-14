package kg.timmitof.keyboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse
import kg.timmitof.keyboard.presentation.model.KeyboardKey
import kg.timmitof.keyboard.presentation.theme.KFTheme

@Composable
fun KeyboardKeyButton(
    key: KeyboardKey,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    onClick: (KeyboardKey) -> Unit
) {
    Box(
        modifier = modifier
            .background(KFTheme.color.keyButtonBackground, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick(key) })
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = key.label,
            fontSize = fontSize.takeOrElse { 20.sp },
            textAlign = TextAlign.Center,
            color = KFTheme.color.keyTextColor
        )
    }
}