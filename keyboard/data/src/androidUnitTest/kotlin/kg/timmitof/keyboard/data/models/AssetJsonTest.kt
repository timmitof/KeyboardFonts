package kg.timmitof.keyboard.data.models

import kg.timmitof.keyboard.data.mapper.KeyboardMapper
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Раскладки из ассетов разбираются целиком: kotlinx.serialization, в отличие от Gson, не пропускает обязательные поля. */
class AssetJsonTest {

    private val layouts = File("src/androidMain/assets/layouts")

    @Test
    fun `каждая раскладка разбирается и не теряет клавиш`() {
        val files = layouts.listFiles { file -> file.extension == "json" && file.name != BOTTOM_ROWS }.orEmpty()
        assertTrue(files.isNotEmpty(), "layouts not found in ${layouts.absolutePath}")

        files.forEach { file ->
            val dto = AssetJson.decodeFromString<KeyboardLayoutDto>(file.readText())
            val layout = with(KeyboardMapper) { dto.toDomain() }

            assertTrue(layout.rows.isNotEmpty(), "${file.name}: no rows")
            assertEquals(dto.rows.map { it.size }, layout.rows.map { it.size }, "${file.name}: keys lost")
        }
    }

    @Test
    fun `нижние ряды разбираются`() {
        val rows = AssetJson.decodeFromString<Map<String, List<KeyboardKeyDto>>>(File(layouts, BOTTOM_ROWS).readText())

        assertTrue(rows.isNotEmpty())
        rows.forEach { (variant, keys) ->
            assertEquals(keys.size, with(KeyboardMapper) { keys.mapNotNull { it.toDomain() } }.size, "$variant: keys lost")
        }
    }

    private companion object {
        const val BOTTOM_ROWS = "bottom_rows.json"
    }
}
