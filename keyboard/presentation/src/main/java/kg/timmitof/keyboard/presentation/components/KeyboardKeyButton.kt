package kg.timmitof.keyboard.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse
import kg.timmitof.keyboard.presentation.model.KeyboardKey

@Composable
fun KeyboardKeyButton(
    key: KeyboardKey,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    onClick: (KeyboardKey) -> Unit
) {
    Button(
        onClick = { onClick(key) },
        modifier = modifier
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(12.dp))
            .shadow(2.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = key.label,
            fontSize = fontSize.takeOrElse { 20.sp },
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}