package kg.timmitof.feature_home.domain.repository

import kg.timmitof.feature_home.domain.model.TemplateModel

interface TemplateRepository {

    suspend fun getAllTemplates(): List<TemplateModel>
}