package kg.timmitof.core.ui.base

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

/**
 * [Container] в нижней шторке. Смахивание и тап мимо идут в [BaseEvent.OnBack];
 * `navigateBack()` прячет шторку с анимацией и зовёт [onDismiss] — тогда её убирают из композиции.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <STATE: BaseState, SIDE_EFFECT: BaseSideEffect.UiSideEffect, EVENT: BaseEvent.UiEvent> SheetContainer(
    viewModel: BaseViewModel<STATE, SIDE_EFFECT, EVENT>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ContainerDSLBuilder<SIDE_EFFECT, EVENT>.(state: State<STATE>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val hide = remember(sheetState, scope) {
        { scope.launch { sheetState.hide() }.invokeOnCompletion { currentOnDismiss() } ; Unit }
    }

    val (state, containerDsl) = bindContainer(viewModel, onNavigateBack = hide)

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = { containerDsl.requestBack(viewModel) },
        sheetState = sheetState,
    ) {
        containerDsl.content(state)
    }
}
