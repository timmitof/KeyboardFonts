package kg.timmitof.feature_home.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kg.timmitof.core.ui.theme.KeyboardFontsTheme
import kg.timmitof.feature_home.presentation.R

@Composable
internal fun TryKeyboardField(modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.setup_try_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TestInputField.entries.forEach { field ->
            TestInputTextField(
                field = field,
                focusManager = focusManager
            )
        }
    }
}

@Composable
private fun TestInputTextField(
    field: TestInputField,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    var text by rememberSaveable(field) { mutableStateOf("") }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (field.singleLine) 0.dp else MultilineFieldMinHeight),
        label = { Text(text = stringResource(field.labelRes)) },
        placeholder = { Text(text = stringResource(R.string.setup_try_placeholder)) },
        shape = MaterialTheme.shapes.large,
        singleLine = field.singleLine,
        visualTransformation = remember(field) {
            if (field.isPassword) PasswordVisualTransformation() else VisualTransformation.None
        },
        keyboardOptions = remember(field) { field.keyboardOptions() },
        // Enter уводит фокус дальше/снимает его — так видно, что imeAction реально сработал.
        keyboardActions = remember(field, focusManager) { focusManager.actionsFor(field) }
    )
}

private val MultilineFieldMinHeight = 120.dp

private enum class TestInputField(
    @StringRes val labelRes: Int,
    val keyboardType: KeyboardType = KeyboardType.Text,
    val imeAction: ImeAction = ImeAction.Default,
    val capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    val singleLine: Boolean = true,
    val isPassword: Boolean = false
) {
    TEXT(
        labelRes = R.string.try_field_text,
        imeAction = ImeAction.Done
    ),
    EMAIL(
        labelRes = R.string.try_field_email,
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Next,
        capitalization = KeyboardCapitalization.None
    ),
    PASSWORD(
        labelRes = R.string.try_field_password,
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Go,
        capitalization = KeyboardCapitalization.None,
        isPassword = true
    ),
    NUMBER(
        labelRes = R.string.try_field_number,
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done,
        capitalization = KeyboardCapitalization.None
    ),
    PHONE(
        labelRes = R.string.try_field_phone,
        keyboardType = KeyboardType.Phone,
        imeAction = ImeAction.Previous,
        capitalization = KeyboardCapitalization.None
    ),
    SEARCH(
        labelRes = R.string.try_field_search,
        imeAction = ImeAction.Search,
        capitalization = KeyboardCapitalization.None
    ),
    MESSAGE(
        labelRes = R.string.try_field_message,
        imeAction = ImeAction.Send
    ),
    MULTILINE(
        labelRes = R.string.try_field_multiline,
        singleLine = false
    );

    fun keyboardOptions(): KeyboardOptions = KeyboardOptions(
        capitalization = capitalization,
        keyboardType = keyboardType,
        imeAction = imeAction
    )
}

private fun FocusManager.actionsFor(field: TestInputField): KeyboardActions = when (field.imeAction) {
    ImeAction.Next -> KeyboardActions(onNext = { moveFocus(FocusDirection.Down) })
    ImeAction.Previous -> KeyboardActions(onPrevious = { moveFocus(FocusDirection.Up) })
    else -> KeyboardActions { clearFocus() }
}

@Preview(showBackground = true)
@Composable
private fun TryKeyboardFieldPreview() {
    KeyboardFontsTheme {
        TryKeyboardField(modifier = Modifier.padding(16.dp))
    }
}
