package kg.timmitof.core.ui.base

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
internal actual fun rememberShowMessage(snackbarHostState: SnackbarHostState): (String) -> Unit {
    val scope = rememberCoroutineScope()
    return remember(snackbarHostState, scope) {
        { message -> scope.launch { snackbarHostState.showSnackbar(message) } }
    }
}
