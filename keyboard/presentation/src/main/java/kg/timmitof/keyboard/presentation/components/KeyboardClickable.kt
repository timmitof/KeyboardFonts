package kg.timmitof.keyboard.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role
import kg.timmitof.keyboard.presentation.components.keys.LocalKeyFeedback

/**
 * Кнопка интерфейса клавиатуры (верхняя строка, панель перевода, листы, карточки): вибрация, как у клавиш,
 * если она включена, и роль для TalkBack. Без ряби — у клавиатуры своя подсветка нажатий.
 */
internal fun Modifier.keyboardClickable(onClick: () -> Unit): Modifier = keyboardClickable(Role.Button, onClick)

internal fun Modifier.keyboardClickable(role: Role, onClick: () -> Unit): Modifier = composed {
    val feedback by rememberUpdatedState(LocalKeyFeedback.current)

    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        role = role,
    ) {
        feedback.onButtonPress()
        onClick()
    }
}
