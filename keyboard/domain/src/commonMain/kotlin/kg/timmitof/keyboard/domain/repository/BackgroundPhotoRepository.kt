package kg.timmitof.keyboard.domain.repository

import kg.timmitof.keyboard.domain.model.BackgroundPhoto
import kg.timmitof.keyboard.domain.model.PhotoCrop
import kotlinx.coroutines.flow.Flow

/** Фото копируются в папку приложения целиком: удаление из галереи их не трогает. */
interface BackgroundPhotoRepository {

    fun observePhotos(): Flow<List<BackgroundPhoto>>

    suspend fun getPhoto(id: Long): BackgroundPhoto?

    /** [uri] — content:// из системного выбора фото. */
    suspend fun addPhoto(uri: String): BackgroundPhoto

    suspend fun setCrop(id: Long, crop: PhotoCrop)

    suspend fun deletePhoto(id: Long)
}
