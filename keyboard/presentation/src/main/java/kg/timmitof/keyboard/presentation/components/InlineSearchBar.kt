package kg.timmitof.keyboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.components.keys.KeyBase
import kg.timmitof.keyboard.presentation.theme.KFTheme

/**
 * Строка поиска внутри клавиатуры (эмодзи, языки перевода): поле с лупой и «✕» клавишей.
 * Текст в поле набирают обычные клавиши — через `InlineEditor`.
 */
@Composable
internal fun InlineSearchBar(
    query: String,
    hint: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    selectionChars: Int = 0,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(SearchBarHeight),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(KFTheme.color.keyButtonBackground, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_search_key),
                contentDescription = null,
                tint = KFTheme.color.keyLabelColor.copy(alpha = MutedAlpha),
                modifier = Modifier.size(15.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            InlineFieldText(
                modifier = Modifier.weight(1f),
                text = query,
                hint = hint,
                selectionChars = selectionChars,
            )
        }

        KeyBase(
            modifier = Modifier
                .width(SearchBarHeight)
                .fillMaxHeight(),
            background = KFTheme.color.keySpecialButtonBackground,
            shadowColor = KFTheme.color.keyButtonShadow,
            onClick = onClose
        ) {
            Text(
                text = "✕",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = KFTheme.color.keySpecialLabelColor
            )
        }
    }
}

private val SearchBarHeight = 42.dp

private const val MutedAlpha = 0.6f
