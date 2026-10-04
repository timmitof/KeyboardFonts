package kg.timmitof.keyboard.data.repository

import kg.timmitof.core.data.local.dao.BackgroundPhotoDao
import kg.timmitof.core.data.local.entities.BackgroundPhotoEntity
import kg.timmitof.keyboard.data.settings.BackgroundPhotoStorage
import kg.timmitof.keyboard.data.settings.toDomain
import kg.timmitof.keyboard.domain.model.BackgroundPhoto
import kg.timmitof.keyboard.domain.model.PhotoCrop
import kg.timmitof.keyboard.domain.repository.BackgroundPhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BackgroundPhotoRepositoryImpl @Inject constructor(
    private val dao: BackgroundPhotoDao,
    private val storage: BackgroundPhotoStorage,
) : BackgroundPhotoRepository {

    override fun observePhotos(): Flow<List<BackgroundPhoto>> =
        dao.observeAll().map { photos -> photos.map { it.toDomain() } }

    override suspend fun getPhoto(id: Long): BackgroundPhoto? = dao.getById(id)?.toDomain()

    override suspend fun addPhoto(uri: String): BackgroundPhoto {
        val stored = storage.copy(uri)
        val entity = BackgroundPhotoEntity(path = stored.path, tone = stored.tone, addedAt = System.currentTimeMillis())
        return entity.copy(id = dao.insert(entity)).toDomain()
    }

    override suspend fun setCrop(id: Long, crop: PhotoCrop) =
        dao.updateCrop(id, crop.left, crop.top, crop.right, crop.bottom)

    override suspend fun deletePhoto(id: Long) {
        val photo = dao.getById(id) ?: return
        dao.delete(id)
        storage.delete(photo.path)
    }
}
