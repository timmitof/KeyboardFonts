package kg.timmitof.keyboard.data

import java.nio.ByteBuffer

interface AssetTextLoader {

    suspend fun loadText(path: String): String?

    /** Бинарный ассет целиком (фильтры, индексы); `null` — файла нет. */
    suspend fun loadBytes(path: String): ByteArray?

    /**
     * Бинарный ассет, отображённый в память без копирования в кучу; `null` — файла нет.
     * Отображается только несжатый ассет (`androidResources.noCompress`), сжатый читается целиком, как [loadBytes].
     */
    suspend fun loadBuffer(path: String): ByteBuffer?
}
