package kg.timmitof.keyboard.data

import java.nio.ByteBuffer

/** Ассеты Android с отображением в память: индексы подсказок читаются без копирования в кучу. */
interface MappedAssetLoader : AssetTextLoader {

    /**
     * Бинарный ассет, отображённый в память без копирования в кучу; `null` — файла нет.
     * Отображается только несжатый ассет (`androidResources.noCompress`), сжатый читается целиком, как [loadBytes].
     */
    suspend fun loadBuffer(path: String): ByteBuffer?
}
