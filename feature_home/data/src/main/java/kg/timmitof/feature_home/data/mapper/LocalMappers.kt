package kg.timmitof.feature_home.data.mapper

import kg.timmitof.core.data.local.entities.TemplateEntity
import kg.timmitof.core.data.local.entities.UserProjectEntity
import kg.timmitof.feature_home.domain.model.TemplateModel
import kg.timmitof.feature_home.domain.model.UserProjectModel

fun TemplateEntity.toDomain() = TemplateModel(
    bitmapFilePath = this.bitmapFilePath,
    name = this.name
)

fun TemplateModel.toEntity() = TemplateEntity(
    bitmapFilePath = this.bitmapFilePath,
    name = this.name
)

fun UserProjectEntity.toDomain() = UserProjectModel(
    bitmapFilePath = this.bitmapFilePath,
    name = this.name
)

fun UserProjectModel.toEntity() = UserProjectEntity(
    bitmapFilePath = this.bitmapFilePath,
    name = this.name
)