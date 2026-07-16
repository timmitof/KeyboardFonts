package kg.timmitof.keyboard.engine

import android.text.InputType
import android.view.inputmethod.EditorInfo
import kg.timmitof.keyboard.presentation.screens.keyboard.states.EnterAction

/**
 * Определяет действие клавиши Enter для текущего поля ввода.
 *
 * [EnterAction.RETURN] (обычный перевод строки), если:
 * - поле явно запретило action флагом [EditorInfo.IME_FLAG_NO_ENTER_ACTION];
 * - поле многострочное — Enter должен вставлять перевод строки;
 * - action не задан.
 */
internal fun EditorInfo?.toEnterAction(): EnterAction {
    if (this == null) return EnterAction.RETURN

    if (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0) return EnterAction.RETURN

    val isMultiline = inputType and InputType.TYPE_MASK_CLASS == InputType.TYPE_CLASS_TEXT &&
        inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE != 0
    if (isMultiline) return EnterAction.RETURN

    return when (imeOptions and EditorInfo.IME_MASK_ACTION) {
        EditorInfo.IME_ACTION_GO -> EnterAction.GO
        EditorInfo.IME_ACTION_SEARCH -> EnterAction.SEARCH
        EditorInfo.IME_ACTION_SEND -> EnterAction.SEND
        EditorInfo.IME_ACTION_NEXT -> EnterAction.NEXT
        EditorInfo.IME_ACTION_PREVIOUS -> EnterAction.PREVIOUS
        EditorInfo.IME_ACTION_DONE -> EnterAction.DONE
        else -> EnterAction.RETURN
    }
}
