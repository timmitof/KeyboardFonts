package kg.timmitof.core.domain.repository

import kg.timmitof.core.domain.model.TemplateModel

interface ProjectRepository {

    suspend fun getAllTemplates(): List<TemplateModel>
}