package kg.timmitof.keyboard.presentation.screens.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState

/**
 * Узкая подписка на кусок состояния: потребитель перерисуется, только когда изменился именно срез
 * (сравнение структурное), а не при любой правке [KeyboardState]. [select] не должен ничего захватывать.
 */
@Composable
internal fun <T> State<KeyboardState>.rememberSlice(select: (KeyboardState) -> T): State<T> =
    remember(this) { derivedStateOf { select(value) } }
