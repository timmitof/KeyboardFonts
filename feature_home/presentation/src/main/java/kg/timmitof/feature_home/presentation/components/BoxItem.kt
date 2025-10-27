package kg.timmitof.feature_home.presentation.components

import KeyboardFonts.feature_home.home.presentation.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Универсальный элемент для списка или сетки
 */
@Preview
@Composable
internal fun BoxItem(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .sizeIn(minWidth = 132.dp, minHeight = 100.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(color)
    )
}

@Preview(showSystemUi = false, showBackground = false)
@Composable
internal fun CreateBackgroundItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .sizeIn(minWidth = 132.dp, minHeight = 100.dp)
            .background(MaterialTheme.colorScheme.background)
            .border(width = 2.dp, color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.create_background),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
    }
}