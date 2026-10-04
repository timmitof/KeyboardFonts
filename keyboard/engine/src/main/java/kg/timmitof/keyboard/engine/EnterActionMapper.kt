package kg.timmitof.keyboard.engine

import android.view.inputmethod.EditorInfo
import kg.timmitof.keyboard.presentation.screens.keyboard.states.EnterAction

/** Поле с [EditorInfo.actionLabel] объявляет действие через [EditorInfo.actionId], а не `imeOptions`. */
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

/** Многострочность не проверяем: такие поля фреймворк сам помечает [EditorInfo.IME_FLAG_NO_ENTER_ACTION]. */
internal fun EditorInfo?.hasEditorAction(): Boolean {
    if (this == null) return false
    if (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0) return false

    val actionId = editorActionId()
    return actionId != EditorInfo.IME_ACTION_UNSPECIFIED && actionId != EditorInfo.IME_ACTION_NONE
}

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
