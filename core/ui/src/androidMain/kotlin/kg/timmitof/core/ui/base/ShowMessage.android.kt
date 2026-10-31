package kg.timmitof.core.ui.base

import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberShowMessage(snackbarHostState: SnackbarHostState): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }
}
