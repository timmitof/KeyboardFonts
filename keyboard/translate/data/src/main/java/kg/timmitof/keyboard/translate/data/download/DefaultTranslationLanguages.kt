package kg.timmitof.keyboard.translate.data.download

import android.content.res.Resources
import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.Locale
import javax.inject.Inject

/**
 * Языки, которые переводчик качает сам, — та же пара, что раскладки по умолчанию: язык системы и английский,
 * а у английской системы — русский. Язык системы, которого переводчик не знает (кыргызский, казахский…), —
 * тоже русский: переводить с него всё равно нельзя, а пара RU ⇄ EN пригодится.
 */
class DefaultTranslationLanguages @Inject constructor() {

    fun codes(): List<String> {
        val system = TranslateLanguage.fromLanguageTag(systemLocale().language)
        val companion = system?.takeIf { it != TranslateLanguage.ENGLISH } ?: TranslateLanguage.RUSSIAN
        return listOf(companion, TranslateLanguage.ENGLISH)
    }

    /** Язык устройства, а не приложения: у приложения может стоять свой язык интерфейса. */
    private fun systemLocale(): Locale =
        Resources.getSystem().configuration.locales.takeIf { !it.isEmpty }?.get(0) ?: Locale.getDefault()
}
