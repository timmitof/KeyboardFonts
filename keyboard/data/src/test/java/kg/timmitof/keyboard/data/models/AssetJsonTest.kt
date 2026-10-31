package kg.timmitof.keyboard.data.models

import kg.timmitof.keyboard.data.mapper.KeyboardMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Раскладки из ассетов разбираются целиком: kotlinx.serialization, в отличие от Gson, не пропускает обязательные поля. */
class AssetJsonTest {

    private val layouts = File("src/main/assets/layouts")

    @Test
    fun `каждая раскладка разбирается и не теряет клавиш`() {
        val files = layouts.listFiles { file -> file.extension == "json" && file.name != BOTTOM_ROWS }.orEmpty()
        assertTrue("layouts not found in ${layouts.absolutePath}", files.isNotEmpty())

        files.forEach { file ->
            val dto = AssetJson.decodeFromString<KeyboardLayoutDto>(file.readText())
            val layout = with(KeyboardMapper) { dto.toDomain() }

            assertTrue("${file.name}: no rows", layout.rows.isNotEmpty())
            assertEquals("${file.name}: keys lost", dto.rows.map { it.size }, layout.rows.map { it.size })
        }
    }

    @Test
    fun `нижние ряды разбираются`() {
        val rows = AssetJson.decodeFromString<Map<String, List<KeyboardKeyDto>>>(File(layouts, BOTTOM_ROWS).readText())

        assertTrue(rows.isNotEmpty())
        rows.forEach { (variant, keys) ->
            assertEquals("$variant: keys lost", keys.size, with(KeyboardMapper) { keys.mapNotNull { it.toDomain() } }.size)
        }
    }

    private companion object {
        const val BOTTOM_ROWS = "bottom_rows.json"
    }
}
