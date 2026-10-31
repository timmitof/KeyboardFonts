package kg.timmitof.keyboard.data.settings

/** Фото фона в файлах приложения: в базе — только путь и кадр. */
interface BackgroundPhotoStorage {

    /** Копия байт в байт: фото остаётся в полном размере и не зависит от галереи. */
    suspend fun copy(uri: String): StoredPhoto

    /** `true`, если файла после вызова нет: по этому признаку вызывающий решает, можно ли забыть ссылку. */
    suspend fun delete(path: String): Boolean

    /** Убирает файл, на который не осталось ссылки (например, не удалась запись в базу). */
    fun discard(path: String)

    data class StoredPhoto(val path: String, val tone: Long)
}
