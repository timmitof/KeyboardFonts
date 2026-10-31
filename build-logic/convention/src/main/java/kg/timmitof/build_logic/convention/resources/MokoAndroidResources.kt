package kg.timmitof.build_logic.convention.resources

import org.w3c.dom.Element
import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

/** Доводка Android-ресурсов, которые генерирует moko-resources из `src/commonMain/moko-resources`. */
internal object MokoAndroidResources {

    private const val BASE = "base"
    private const val STRINGS_FILE = "strings.xml"
    private const val OUTPUT_FILE = "multiplatform_strings.xml"

    /**
     * moko строит каталог Android из языка и страны и теряет скрипт: `uz-Cyrl` пишется в `values-uz`
     * поверх латиницы. Локали со скриптом кладутся в `values-b+uz+Cyrl`, а `values-uz` пересобирается
     * из своей локали. Значения экранируются так же, как у moko.
     */
    fun writeScriptLocales(sourceDir: File, outputResDir: File) {
        val locales = sourceDir.listFiles().orEmpty()
            .filter { it.name != BASE && File(it, STRINGS_FILE).exists() }
            .associate { it.name to Locale.forLanguageTag(it.name) }

        locales.filterValues { it.script.isNotEmpty() }.forEach { (name, locale) ->
            writeStrings(File(sourceDir, "$name/$STRINGS_FILE"), File(outputResDir, locale.qualifiedValuesDir()))

            // moko отдал этой локали каталог языка без скрипта — возвращаем его владельцу.
            val plain = locales.entries.firstOrNull { (_, other) -> other == Locale.forLanguageTag(locale.language) }
            if (plain != null) {
                writeStrings(File(sourceDir, "${plain.key}/$STRINGS_FILE"), File(outputResDir, "values-${locale.language}"))
            }
        }
    }

    /** moko не переносит `translatable="false"`, и lint требует перевода названий и образцов текста. */
    fun markUntranslatable(sourceDir: File, outputResDir: File) {
        val untranslatable = File(sourceDir, "$BASE/$STRINGS_FILE").stringElements()
            .filter { it.getAttribute("translatable") == "false" }
            .map { it.getAttribute("name") }
            .toSet()
        if (untranslatable.isEmpty()) return

        val output = File(outputResDir, "values/$OUTPUT_FILE")
        output.writeText(
            output.readText().replace(Regex("<string name=\"([^\"]+)\">")) { match ->
                if (match.groupValues[1] in untranslatable) {
                    "<string name=\"${match.groupValues[1]}\" translatable=\"false\">"
                } else {
                    match.value
                }
            }
        )
    }

    private fun Locale.qualifiedValuesDir(): String = buildString {
        append("values-b+").append(language).append('+').append(script)
        if (country.isNotEmpty()) append('+').append(country)
    }

    private fun writeStrings(source: File, valuesDir: File) {
        val content = source.stringElements().joinToString("\n") { element ->
            "\t<string name=\"${element.getAttribute("name")}\">${element.textContent.toAndroidValue()}</string>"
        }

        valuesDir.mkdirs()
        File(valuesDir, OUTPUT_FILE).writeText(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n$content\n</resources>"
        )
    }

    private fun File.stringElements(): List<Element> {
        val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this)
            .getElementsByTagName("string")
        return (0 until nodes.length).map { nodes.item(it) as Element }
    }

    /** Как в moko: снять Android-экранирование и переносы строк IDE, затем экранировать заново. */
    private fun String.toAndroidValue(): String {
        val value = replace("\\'", "'")
            .replace("\\\"", "\"")
            .replace("\\?", "?")
            .replace("\\@", "@")
            .replace(Regex("\\s*\n\\s*"), " ")
            .replace("\\n", "\n")

        val escaped = value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "\\&quot;")
            .replace("'", "\\&apos;")
            .replace("\n", "\\n")

        return if (escaped.firstOrNull() == '@' || escaped.firstOrNull() == '?') "\\$escaped" else escaped
    }
}
