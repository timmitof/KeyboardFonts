package kg.timmitof.keyboard.data

interface AssetTextLoader {

    suspend fun loadText(path: String): String?

    /** Бинарный ассет целиком (фильтры, индексы); `null` — файла нет. */
    suspend fun loadBytes(path: String): ByteArray?
}
