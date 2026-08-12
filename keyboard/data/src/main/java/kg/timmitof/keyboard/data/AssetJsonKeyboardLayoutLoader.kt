package kg.timmitof.keyboard.data

import javax.inject.Inject

class AssetJsonKeyboardLayoutLoader @Inject constructor(
    private val assetTextLoader: AssetTextLoader
) : JsonKeyboardLayoutLoader {

    override suspend fun loadKeyboardLayout(filename: String): String? =
        assetTextLoader.loadText("$KEYBOARD_LAYOUT_FOLDER/$filename")

    companion object {
        private const val KEYBOARD_LAYOUT_FOLDER = "layouts"
    }
}
