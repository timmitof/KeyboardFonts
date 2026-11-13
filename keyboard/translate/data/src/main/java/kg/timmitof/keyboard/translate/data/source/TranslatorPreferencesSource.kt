package kg.timmitof.keyboard.translate.data.source

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kg.timmitof.keyboard.translate.domain.model.TranslatorPreferences
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** Свой файл: переводчик живёт только в клавиатуре, экрану настроек в другом процессе эти ключи не нужны. */
private val Context.translatorPreferences by preferencesDataStore(name = "translator_preferences")

@Singleton
class TranslatorPreferencesSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    suspend fun get(): TranslatorPreferences {
        val prefs = context.translatorPreferences.data.first()
        return TranslatorPreferences(
            targetCode = prefs[TARGET_KEY],
            appTargets = decodeAppTargets(prefs[APP_TARGETS_KEY]),
            recentTargets = prefs[RECENT_TARGETS_KEY]?.split(SEPARATOR)?.filter(String::isNotEmpty).orEmpty(),
            isAutoSource = prefs[AUTO_SOURCE_KEY] ?: false,
            isHintShown = prefs[HINT_SHOWN_KEY] ?: false,
        )
    }

    suspend fun setTarget(code: String, packageName: String?) {
        context.translatorPreferences.edit { prefs ->
            prefs[TARGET_KEY] = code
            if (packageName != null) {
                // Свежее приложение — первым: при переполнении забываем те, где давно не переводили.
                val others = decodeAppTargets(prefs[APP_TARGETS_KEY]).filterKeys { it != packageName }
                prefs[APP_TARGETS_KEY] = encodeAppTargets(mapOf(packageName to code) + others.entries.take(MAX_APPS - 1).associate { it.toPair() })
            }
            // Список, а не множество: порядок — от свежего к старому.
            val recent = prefs[RECENT_TARGETS_KEY]?.split(SEPARATOR).orEmpty().filter { it.isNotEmpty() && it != code }
            prefs[RECENT_TARGETS_KEY] = (listOf(code) + recent).take(MAX_RECENT).joinToString(SEPARATOR)
        }
    }

    suspend fun setHintShown() {
        context.translatorPreferences.edit { it[HINT_SHOWN_KEY] = true }
    }

    suspend fun setAutoSource(isAuto: Boolean) {
        context.translatorPreferences.edit { it[AUTO_SOURCE_KEY] = isAuto }
    }

    /** Языки, которые пользователь удалил сам: фоновая загрузка по умолчанию их обратно не качает. */
    suspend fun declinedModels(): Set<String> =
        context.translatorPreferences.data.first()[DECLINED_KEY].orEmpty()

    suspend fun setDeclined(code: String, isDeclined: Boolean) {
        context.translatorPreferences.edit { prefs ->
            val declined = prefs[DECLINED_KEY].orEmpty()
            prefs[DECLINED_KEY] = if (isDeclined) declined + code else declined - code
        }
    }

    /** «пакет=код» через запятую: ни в пакетах, ни в кодах языков этих символов нет. */
    private fun decodeAppTargets(raw: String?): Map<String, String> =
        raw?.split(SEPARATOR)
            ?.mapNotNull { entry -> entry.split(PAIR_SEPARATOR).takeIf { it.size == 2 }?.let { it[0] to it[1] } }
            ?.toMap(LinkedHashMap())
            .orEmpty()

    private fun encodeAppTargets(targets: Map<String, String>): String =
        targets.entries.joinToString(SEPARATOR) { (app, code) -> "$app$PAIR_SEPARATOR$code" }

    private companion object {
        val TARGET_KEY = stringPreferencesKey("target_language")
        val HINT_SHOWN_KEY = booleanPreferencesKey("hint_shown")
        val AUTO_SOURCE_KEY = booleanPreferencesKey("auto_source")
        val RECENT_TARGETS_KEY = stringPreferencesKey("recent_targets")
        const val SEPARATOR = ","
        const val PAIR_SEPARATOR = "="
        val APP_TARGETS_KEY = stringPreferencesKey("app_targets")

        /** Сколько приложений помним: пар немного, а строка в настройках не должна расти бесконечно. */
        const val MAX_APPS = 50
        const val MAX_RECENT = 4
        val DECLINED_KEY = stringSetPreferencesKey("declined_models")
    }
}
