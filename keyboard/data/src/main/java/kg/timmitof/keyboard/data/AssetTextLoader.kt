package kg.timmitof.keyboard.data

/** Чтение текстовых файлов из `assets`. */
interface AssetTextLoader {

    /**
     * @param path путь внутри `assets`, например `emoji/annotations.tsv`.
     * @return содержимое файла или `null`, если файла нет.
     */
    suspend fun loadText(path: String): String?
}
