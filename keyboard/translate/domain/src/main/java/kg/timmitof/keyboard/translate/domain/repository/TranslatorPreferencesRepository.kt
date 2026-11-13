package kg.timmitof.keyboard.translate.domain.repository

import kg.timmitof.keyboard.translate.domain.model.TranslatorPreferences

/** Что переводчик помнит между открытиями: язык перевода и шрифт для него. */
interface TranslatorPreferencesRepository {

    suspend fun getPreferences(): TranslatorPreferences

    suspend fun setTarget(code: String)

    suspend fun setStyled(isStyled: Boolean)
}
