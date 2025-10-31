package kg.timmitof.feature_home.presentation.components

import KeyboardFonts.feature_home.home.presentation.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeTopAppBar(
    modifier: Modifier = Modifier,
    onOpenKeyboardSettings: () -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        title = {},
        actions = {
            IconButton(onClick = onOpenKeyboardSettings) {
                Image(
                    painter = painterResource(R.drawable.ic_keyboard_off),
                    contentDescription = stringResource(R.string.content_description_keyboard_off)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp
        )
    )
}

@Preview
@Composable
private fun Preview() {
    HomeTopAppBar()
}