package kg.timmitof.keyboard.translate.data.repository

import kg.timmitof.keyboard.translate.data.source.TranslatorPreferencesSource
import kg.timmitof.keyboard.translate.domain.model.TranslatorPreferences
import kg.timmitof.keyboard.translate.domain.repository.TranslatorPreferencesRepository
import javax.inject.Inject

class TranslatorPreferencesRepositoryImpl @Inject constructor(
    private val source: TranslatorPreferencesSource,
) : TranslatorPreferencesRepository {

    override suspend fun getPreferences(): TranslatorPreferences = source.get()

    override suspend fun setTarget(code: String) = source.setTarget(code)

    override suspend fun setHintShown() = source.setHintShown()

    override suspend fun setAutoSource(isAuto: Boolean) = source.setAutoSource(isAuto)
}
