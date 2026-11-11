package kg.timmitof.keyboard.suggestion.domain.model

/** Что пользователь сделал с работой Т9 — по этим событиям видно, помогает автозамена или мешает. */
sealed interface TypingEvent {

    /** Слово закончено пробелом или знаком и осталось как набрано. */
    data object WordKept : TypingEvent

    /** Слово заменено автозаменой. */
    data object AutoCorrected : TypingEvent

    /** Автозамену откатили Backspace'ом — исправление было лишним. */
    data object AutoCorrectionUndone : TypingEvent

    /**
     * Сразу после оставленного как есть слова нажат Backspace — скорее всего, в слове ошибка,
     * которую автозамена пропустила. Оценка грубая: так же стирают и пробел, чтобы дописать слово.
     */
    data object KeptWordErased : TypingEvent

    /** Выбрана подсказка к набираемому слову; [position] — место в списке движка, 0 — само набранное. */
    data class SuggestionPicked(val position: Int) : TypingEvent

    /** Выбрано предсказание следующего слова. */
    data object PredictionPicked : TypingEvent

    /** Умные зоны напечатали соседа вместо нажатой буквы. */
    data object KeyAdapted : TypingEvent

    /** Подменённую букву сразу стёрли — подмена была ошибкой. */
    data object KeyAdaptationUndone : TypingEvent
}
