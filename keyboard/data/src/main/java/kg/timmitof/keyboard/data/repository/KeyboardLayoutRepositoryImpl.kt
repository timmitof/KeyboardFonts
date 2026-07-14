package kg.timmitof.keyboard.data.repository

import com.google.gson.Gson
import kg.timmitof.keyboard.data.JsonKeyboardLayoutLoader
import kg.timmitof.keyboard.data.mapper.KeyboardMapper
import kg.timmitof.keyboard.data.models.KeyboardLayoutDto
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class KeyboardLayoutRepositoryImpl @Inject constructor(
    private val loader: JsonKeyboardLayoutLoader
) : KeyboardLayoutRepository {

    private val gson = Gson()
    private val cache = ConcurrentHashMap<String, KeyboardLayout>()

    override suspend fun getLayout(language: String): KeyboardLayout {
        cache[language]?.let { return it }

        val filename = "$language.json"

        val jsonText = loader.loadKeyboardLayout(filename)
            ?: throw IllegalStateException("Keyboard layout not found: $filename")

        val dto = gson.fromJson(jsonText, KeyboardLayoutDto::class.java)

        val layout = with(KeyboardMapper) { dto.toDomain() }

        cache[language] = layout

        return layout
    }
}