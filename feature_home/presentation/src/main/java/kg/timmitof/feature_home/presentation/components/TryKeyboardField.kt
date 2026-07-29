package kg.timmitof.feature_home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kg.timmitof.core.ui.theme.KeyboardFontsTheme
import kg.timmitof.feature_home.presentation.R

/**
 * Поле «попробуй здесь» — быстрый способ проверить, что клавиатура действительно подключилась,
 * не выходя из приложения.
 */
@Composable
internal fun TryKeyboardField(modifier: Modifier = Modifier) {
    var text by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.setup_try_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = stringResource(R.string.setup_try_placeholder)) },
            shape = MaterialTheme.shapes.large,
            singleLine = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TryKeyboardFieldPreview() {
    KeyboardFontsTheme {
        TryKeyboardField(modifier = Modifier.padding(16.dp))
    }
}
