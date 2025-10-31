package kg.timmitof.feature_home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> TwoColumnGrid(
    modifier: Modifier = Modifier,
    items: List<T>,
    horizontalSpacing: Dp = 10.dp,
    verticalSpacing: Dp = 14.dp,
    content: @Composable (T) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(verticalSpacing)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)) {
                rowItems.forEach {
                    Box(modifier = Modifier.weight(1f)) { content(it) }
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}