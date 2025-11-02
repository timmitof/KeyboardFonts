package kg.timmitof.feature_home.data.repository

import kg.timmitof.feature_home.data.mapper.toDomain
import kg.timmitof.feature_home.data.source.TemplateLocalDataSource
import kg.timmitof.feature_home.domain.model.TemplateModel
import kg.timmitof.feature_home.domain.repository.TemplateRepository
import javax.inject.Inject

class TemplateRepositoryImpl @Inject constructor(
    private val templateLocalDataSource: TemplateLocalDataSource
) : TemplateRepository {

    override suspend fun getAllTemplates(): List<TemplateModel> =
        templateLocalDataSource.getAllTemplates().map { it.toDomain() }
}