package kg.timmitof.feature_splash.domain.repository

interface TemplateRepository {

    suspend fun loadTemplatesFromAsset()

    suspend fun getTemplateCount(): Int
}