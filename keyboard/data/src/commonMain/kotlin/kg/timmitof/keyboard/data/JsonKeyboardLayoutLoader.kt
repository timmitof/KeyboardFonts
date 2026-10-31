package kg.timmitof.keyboard.data

interface JsonKeyboardLayoutLoader {
    suspend fun loadKeyboardLayout(filename: String): String?
}