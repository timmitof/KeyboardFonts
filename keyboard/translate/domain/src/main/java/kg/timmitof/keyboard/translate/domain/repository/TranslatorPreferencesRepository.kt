package kg.timmitof.keyboard.translate.domain.repository

import kg.timmitof.keyboard.translate.domain.model.TranslatorPreferences

/** Что переводчик помнит между открытиями: язык перевода и показанную подсказку. */
interface TranslatorPreferencesRepository {

    suspend fun getPreferences(): TranslatorPreferences

    /** Запоминает и как текущий язык перевода, и как самый свежий из недавних. */
    suspend fun setTarget(code: String)

    suspend fun setHintShown()

    suspend fun setAutoSource(isAuto: Boolean)
}
