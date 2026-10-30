package kg.timmitof.keyboard.data


class AssetJsonKeyboardLayoutLoader(
    private val assetTextLoader: AssetTextLoader
) : JsonKeyboardLayoutLoader {

    override suspend fun loadKeyboardLayout(filename: String): String? =
        assetTextLoader.loadText("$KEYBOARD_LAYOUT_FOLDER/$filename")

    companion object {
        private const val KEYBOARD_LAYOUT_FOLDER = "layouts"
    }
}
