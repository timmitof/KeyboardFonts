package kg.timmitof.keyboard.data.settings

import kg.timmitof.core.data.local.entities.BackgroundPhotoEntity
import kg.timmitof.keyboard.domain.model.BackgroundPhoto
import kg.timmitof.keyboard.domain.model.PhotoCrop

internal fun BackgroundPhotoEntity.toDomain() = BackgroundPhoto(
    id = id,
    path = path,
    tone = tone,
    crop = PhotoCrop(cropLeft, cropTop, cropRight, cropBottom),
)
