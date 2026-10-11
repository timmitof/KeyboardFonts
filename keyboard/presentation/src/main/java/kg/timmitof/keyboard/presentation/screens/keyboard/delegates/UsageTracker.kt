package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.core.analytics.Analytics
import kg.timmitof.core.analytics.AnalyticsEvent
import kg.timmitof.core.analytics.AnalyticsEvents
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState

/**
 * Использование клавиатуры: счётчики за сессию ввода (одно поле) уходят одним событием при её завершении,
 * а не на каждое нажатие. Сам текст не хранится. Intent-ы Orbit идут параллельно — счётчики под `synchronized`.
 */
internal class UsageTracker(
    private val analytics: Analytics,
) {

    private var chars = 0
    private var styledChars = 0
    private var words = 0
    private var suggestionsPicked = 0
    private var autoCorrections = 0
    private var autoCorrectionsUndone = 0
    private var emojis = 0
    private var clipboardPastes = 0

    /** Редкие действия — выбор шрифта, языка, панели — уходят сразу. */
    fun log(event: AnalyticsEvent) = analytics.log(event)

    @Synchronized
    fun onCharacter(isStyled: Boolean) {
        chars++
        if (isStyled) styledChars++
    }

    @Synchronized
    fun onWord() {
        words++
    }

    @Synchronized
    fun onSuggestionPicked() {
        suggestionsPicked++
        words++
    }

    @Synchronized
    fun onAutoCorrection() {
        autoCorrections++
    }

    @Synchronized
    fun onAutoCorrectionUndone() {
        autoCorrectionsUndone++
    }

    @Synchronized
    fun onEmoji() {
        emojis++
    }

    @Synchronized
    fun onClipboardPaste() {
        clipboardPastes++
    }

    /** Поле открыли и ничего не ввели — такую сессию не отправляем. */
    @Synchronized
    fun finishSession(state: KeyboardState) {
        val hasInput = chars + emojis + clipboardPastes + suggestionsPicked > 0
        if (hasInput) {
            analytics.log(
                AnalyticsEvents.Keyboard.session(
                    chars = chars,
                    styledChars = styledChars,
                    words = words,
                    suggestionsPicked = suggestionsPicked,
                    autoCorrections = autoCorrections,
                    autoCorrectionsUndone = autoCorrectionsUndone,
                    emojis = emojis,
                    clipboardPastes = clipboardPastes,
                    language = state.activeLanguage?.code.orEmpty(),
                    font = state.activeFont.id,
                    fieldType = state.fieldType.name.lowercase(),
                )
            )
        }
        reset()
    }

    private fun reset() {
        chars = 0
        styledChars = 0
        words = 0
        suggestionsPicked = 0
        autoCorrections = 0
        autoCorrectionsUndone = 0
        emojis = 0
        clipboardPastes = 0
    }
}
