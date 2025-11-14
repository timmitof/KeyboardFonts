package kg.timmitof.keyboard.data.repository

import com.google.gson.Gson
import kg.timmitof.keyboard.data.JsonKeyboardLayoutLoader
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import javax.inject.Inject

class KeyboardLayoutRepositoryImpl @Inject constructor(
    private val loader: JsonKeyboardLayoutLoader,
    private val gson: Gson = Gson()
) : KeyboardLayoutRepository {

    // memory cache
    private val cache = mutableMapOf<String, KeyboardLayout>()

    override suspend fun getLayout(language: String): KeyboardLayout {
        cache[language]?.let { return it }

        val filename = "$language.json"

        val jsonText = loader.loadKeyboardLayout(filename)
            ?: throw IllegalStateException("Keyboard layout not found: $filename")

        val layout = gson.fromJson(jsonText, KeyboardLayout::class.java)

        cache[language] = layout

        return layout
    }
}