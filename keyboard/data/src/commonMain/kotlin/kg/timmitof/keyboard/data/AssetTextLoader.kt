package kg.timmitof.keyboard.data

interface AssetTextLoader {

    suspend fun loadText(path: String): String?

    /** Бинарный ассет целиком (фильтры, индексы); `null` — файла нет. */
    suspend fun loadBytes(path: String): ByteArray?

    /**
     * Бинарный ассет, отображённый в память без копирования в кучу; `null` — файла нет.
     * На Android отображается только несжатый ассет (`androidResources.noCompress`), сжатый читается целиком.
     */
    suspend fun loadMapped(path: String): MappedBytes?
}
