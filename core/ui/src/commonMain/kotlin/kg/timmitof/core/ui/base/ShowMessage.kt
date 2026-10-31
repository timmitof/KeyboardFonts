package kg.timmitof.core.ui.base

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable

/** Короткое сообщение поверх экрана. На Android — системный тост; на iOS тостов нет — снекбар [snackbarHostState]. */
@Composable
internal expect fun rememberShowMessage(snackbarHostState: SnackbarHostState): (String) -> Unit
