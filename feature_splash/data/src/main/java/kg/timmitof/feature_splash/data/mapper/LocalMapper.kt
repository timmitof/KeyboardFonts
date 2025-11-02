package kg.timmitof.feature_splash.data.mapper

import kg.timmitof.core.data.local.entities.TemplateEntity
import kg.timmitof.feature_splash.domain.model.TemplateModel

internal fun TemplateEntity.toDomain() = TemplateModel(
    bitmapFilePath = this.bitmapFilePath,
    name = this.name
)

internal fun TemplateModel.toEntity() = TemplateEntity(
    bitmapFilePath = this.bitmapFilePath,
    name = this.name
)