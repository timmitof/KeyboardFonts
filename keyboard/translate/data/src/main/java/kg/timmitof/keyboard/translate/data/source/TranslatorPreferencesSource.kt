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
            recentTargets = prefs[RECENT_TARGETS_KEY]?.split(SEPARATOR)?.filter(String::isNotEmpty).orEmpty(),
            isAutoSource = prefs[AUTO_SOURCE_KEY] ?: false,
            isHintShown = prefs[HINT_SHOWN_KEY] ?: false,
        )
    }

    suspend fun setTarget(code: String) {
        context.translatorPreferences.edit { prefs ->
            prefs[TARGET_KEY] = code
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

    private companion object {
        val TARGET_KEY = stringPreferencesKey("target_language")
        val HINT_SHOWN_KEY = booleanPreferencesKey("hint_shown")
        val AUTO_SOURCE_KEY = booleanPreferencesKey("auto_source")
        val RECENT_TARGETS_KEY = stringPreferencesKey("recent_targets")
        const val SEPARATOR = ","
        const val MAX_RECENT = 4
        val DECLINED_KEY = stringSetPreferencesKey("declined_models")
    }
}
