package kg.timmitof.keyboard.presentation.screens.keyboard.states

import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.translate.domain.model.DetectedLanguage

/**
 * Когда верить определению языка. ML Kit на коротком тексте путает близкие языки (русский, кыргызский,
 * казахский), поэтому подсказываем только по нескольким словам, с запасом уверенности и только
 * среди включённых раскладок — молча ничего не переключаем.
 */
internal object LanguageDetectionRules {

    /** Язык понятен и по концу сообщения: длинный текст целиком определять незачем. */
    const val MAX_TEXT_LENGTH = 200

    /**
     * Есть ли что подсказывать: другая включённая раскладка с тем же алфавитом. При паре RU + EN на русской
     * раскладке латиницей не напечатать — определять язык незачем, и он не определяется вовсе.
     */
    fun canSuggestLayout(current: KeyboardLanguage?, enabled: List<KeyboardLanguage>): Boolean =
        current != null && enabled.any { it.code != current.code && it.isLatin == current.isLatin }

    /** Слишком короткий текст не определяем: «ок», «да» одинаковы во многих языках. */
    fun isLongEnough(text: String): Boolean {
        val letters = text.count(Char::isLetter)
        val words = text.split(WHITESPACE).count { word -> word.any(Char::isLetter) }
        return letters >= MIN_LETTERS && words >= MIN_WORDS
    }

    /**
     * Включённая раскладка, на языке которой, похоже, набран текст; null — текущая подходит или уверенности мало.
     * Из нескольких раскладок одного языка (узбекский на латинице и кириллице) — та, что совпадает по алфавиту.
     */
    fun suggestLayout(
        detected: List<DetectedLanguage>,
        current: KeyboardLanguage?,
        enabled: List<KeyboardLanguage>,
        text: String,
    ): KeyboardLanguage? {
        val enabledCodes = enabled.mapTo(mutableSetOf()) { it.baseCode }
        val best = detected.firstOrNull { it.code in enabledCodes } ?: return null
        val currentCode = current?.baseCode

        if (best.code == currentCode || best.confidence < MIN_CONFIDENCE) return null
        val currentConfidence = detected.firstOrNull { it.code == currentCode }?.confidence ?: 0f
        if (currentConfidence > MAX_CURRENT_CONFIDENCE) return null

        val candidates = enabled.filter { it.baseCode == best.code }
        val isLatinText = text.filter(Char::isLetter).let { letters -> letters.count { it.code < LATIN_LIMIT } * 2 > letters.length }
        return candidates.firstOrNull { it.isLatin == isLatinText } ?: candidates.firstOrNull()
    }

    /**
     * Самый вероятный язык, если уверенность достаточная: исходный в режиме «определять автоматически»
     * и язык скопированного текста.
     */
    fun confidentCode(detected: List<DetectedLanguage>): String? =
        detected.firstOrNull()?.takeIf { it.confidence >= MIN_CONFIDENCE }?.code

    /** Базовый язык раскладки: `uz_latn` → `uz`, `ru_ru` → `ru`. */
    val KeyboardLanguage.baseCode: String get() = code.substringBefore('_').lowercase()

    private val WHITESPACE = Regex("\\s+")
    private const val MIN_LETTERS = 10
    private const val MIN_WORDS = 2
    private const val MIN_CONFIDENCE = 0.5f
    private const val MAX_CURRENT_CONFIDENCE = 0.3f

    /** Латиница с диакритикой (ş, ğ, oʻ) — до кириллицы. */
    private const val LATIN_LIMIT = 0x0400
}

/** Запрос определения языка; меняется раскладка или режим — определяем заново тот же текст. */
internal data class DetectionRequest(
    val text: String,
    val layoutCode: String?,
    val isAutoSource: Boolean,
)
