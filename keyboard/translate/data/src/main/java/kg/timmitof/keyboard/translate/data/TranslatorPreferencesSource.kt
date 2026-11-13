package kg.timmitof.keyboard.translate.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
            isStyled = prefs[STYLED_KEY] ?: true,
        )
    }

    suspend fun setTarget(code: String) {
        context.translatorPreferences.edit { it[TARGET_KEY] = code }
    }

    suspend fun setStyled(isStyled: Boolean) {
        context.translatorPreferences.edit { it[STYLED_KEY] = isStyled }
    }

    private companion object {
        val TARGET_KEY = stringPreferencesKey("target_language")
        val STYLED_KEY = booleanPreferencesKey("styled_translation")
    }
}
