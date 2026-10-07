package kg.timmitof.keyboard.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kg.timmitof.keyboard.data.JsonKeyboardLayoutLoader
import kg.timmitof.keyboard.data.mapper.KeyboardMapper
import kg.timmitof.keyboard.data.models.KeyboardKeyDto
import kg.timmitof.keyboard.data.models.KeyboardLayoutDto
import kg.timmitof.keyboard.domain.model.KeyboardKey
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class KeyboardLayoutRepositoryImpl @Inject constructor(
    private val loader: JsonKeyboardLayoutLoader
) : KeyboardLayoutRepository {

    private val gson = Gson()
    private val cache = ConcurrentHashMap<String, KeyboardLayout>()

    // Один JSON не разбираем дважды при параллельных запросах.
    private val mutex = Mutex()

    /** Все варианты нижнего ряда лежат в одном ассете — грузим и разбираем один раз. */
    @Volatile
    private var bottomRows: Map<String, List<KeyboardKey>>? = null

    override suspend fun getLayout(language: String): KeyboardLayout {
        cache[language]?.let { return it }

        return mutex.withLock {
            cache[language] ?: loadLayout(language).also { cache[language] = it }
        }
    }

    private suspend fun loadLayout(language: String): KeyboardLayout {
        val filename = "$language.json"

        val jsonText = loader.loadKeyboardLayout(filename)
            ?: throw IllegalStateException("Keyboard layout not found: $filename")

        return withContext(Dispatchers.Default) {
            val dto = gson.fromJson(jsonText, KeyboardLayoutDto::class.java)
            with(KeyboardMapper) { dto.toDomain() }
        }
    }

    override suspend fun getBottomRow(variant: String): List<KeyboardKey>? =
        loadBottomRows()[variant]

    private suspend fun loadBottomRows(): Map<String, List<KeyboardKey>> {
        bottomRows?.let { return it }

        return mutex.withLock {
            bottomRows ?: parseBottomRows().also { bottomRows = it }
        }
    }

    private suspend fun parseBottomRows(): Map<String, List<KeyboardKey>> {
        val jsonText = loader.loadKeyboardLayout(BOTTOM_ROWS_FILE)

        return withContext(Dispatchers.Default) {
            val dto: Map<String, List<KeyboardKeyDto>> = jsonText
                ?.let { gson.fromJson<Map<String, List<KeyboardKeyDto>>>(it, bottomRowsType) }
                .orEmpty()

            dto.mapValues { (_, row) ->
                with(KeyboardMapper) { row.mapNotNull { it.toDomain() } }
            }
        }
    }

    private companion object {
        const val BOTTOM_ROWS_FILE = "bottom_rows.json"

        val bottomRowsType = object : TypeToken<Map<String, List<KeyboardKeyDto>>>() {}.type
    }
}
