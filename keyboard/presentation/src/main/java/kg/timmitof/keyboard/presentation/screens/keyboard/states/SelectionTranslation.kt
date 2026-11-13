package kg.timmitof.keyboard.presentation.screens.keyboard.states

import androidx.compose.runtime.Immutable

/**
 * Перевод выделенного в поле приложения — что сейчас с ним происходит. Относится к конкретному тексту:
 * сменили выделение — статус старого больше не показываем.
 */
@Immutable
internal data class SelectionTranslation(
    val text: String,
    val status: SelectionTranslationStatus,
) {
    fun isWorkingOn(selected: String): Boolean = text == selected && status == SelectionTranslationStatus.TRANSLATING
}

internal enum class SelectionTranslationStatus {
    TRANSLATING,
    /** Языка нет на телефоне — поставили в загрузку; нажать ещё раз, когда скачается. */
    DOWNLOADING,
    UNSUPPORTED,
    FAILED,
}
