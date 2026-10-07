package kg.timmitof.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Фото фона: файл лежит в `filesDir/backgrounds`, кадр — доли сторон исходной картинки. */
@Entity(tableName = BackgroundPhotoEntity.TABLE)
data class BackgroundPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val path: String,
    val tone: Long,
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 1f,
    val cropBottom: Float = 1f,
    val addedAt: Long,
) {
    companion object {
        const val TABLE = "background_photos"
    }
}
