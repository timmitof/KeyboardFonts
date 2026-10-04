package kg.timmitof.keyboard.data

interface AssetTextLoader {

    suspend fun loadText(path: String): String?
}
