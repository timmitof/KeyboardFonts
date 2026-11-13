package kg.timmitof.keyboard.translate.domain.repository

import kg.timmitof.keyboard.translate.domain.model.TranslatorPreferences

/** Что переводчик помнит между открытиями: язык перевода и показанную подсказку. */
interface TranslatorPreferencesRepository {

    suspend fun getPreferences(): TranslatorPreferences

    /**
     * Запоминает как текущий язык перевода, как самый свежий из недавних и — если известно приложение —
     * как язык перевода этого приложения.
     */
    suspend fun setTarget(code: String, packageName: String? = null)

    suspend fun setHintShown()

    suspend fun setAutoSource(isAuto: Boolean)
}
