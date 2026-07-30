package kg.timmitof.keyboard.engine

import android.view.inputmethod.EditorInfo
import kg.timmitof.keyboard.presentation.screens.keyboard.states.EnterAction

/**
 * Идентификатор действия редактора, который нужно отправлять в `performEditorAction`.
 *
 * Поле с собственной меткой кнопки ([EditorInfo.actionLabel]) объявляет действие через
 * [EditorInfo.actionId], а не через `imeOptions` — там оно остаётся `UNSPECIFIED`.
 */
internal fun EditorInfo?.editorActionId(): Int {
    if (this == null) return EditorInfo.IME_ACTION_UNSPECIFIED

    val optionsAction = imeOptions and EditorInfo.IME_MASK_ACTION
    return when {
        actionLabel != null && actionId != 0 -> actionId
        optionsAction != EditorInfo.IME_ACTION_UNSPECIFIED -> optionsAction
        // Некоторые поля заполняют только actionId, оставляя imeOptions пустым
        actionId != 0 -> actionId
        else -> EditorInfo.IME_ACTION_UNSPECIFIED
    }
}

/**
 * Есть ли у поля действие, которое Enter должен выполнять вместо перевода строки.
 *
 * Многострочность отдельно не проверяем: такие поля фреймворк сам помечает
 * [EditorInfo.IME_FLAG_NO_ENTER_ACTION] (`TextView.shouldAdvanceFocusOnEnter`,
 * `BasicTextField` при `ImeAction.Default`). Если флага нет, а действие задано —
 * поле осознанно просит именно действие, и перебивать его нельзя.
 */
internal fun EditorInfo?.hasEditorAction(): Boolean {
    if (this == null) return false
    if (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0) return false

    val actionId = editorActionId()
    return actionId != EditorInfo.IME_ACTION_UNSPECIFIED && actionId != EditorInfo.IME_ACTION_NONE
}

/**
 * Определяет действие клавиши Enter для текущего поля ввода.
 *
 * [EnterAction.RETURN] (обычный перевод строки), если поле не просит действие
 * (см. [hasEditorAction]) или просит нестандартное — своей иконки для него нет.
 */
internal fun EditorInfo?.toEnterAction(): EnterAction {
    if (!hasEditorAction()) return EnterAction.RETURN

    return when (editorActionId()) {
        EditorInfo.IME_ACTION_GO -> EnterAction.GO
        EditorInfo.IME_ACTION_SEARCH -> EnterAction.SEARCH
        EditorInfo.IME_ACTION_SEND -> EnterAction.SEND
        EditorInfo.IME_ACTION_NEXT -> EnterAction.NEXT
        EditorInfo.IME_ACTION_PREVIOUS -> EnterAction.PREVIOUS
        EditorInfo.IME_ACTION_DONE -> EnterAction.DONE
        else -> EnterAction.RETURN
    }
}
