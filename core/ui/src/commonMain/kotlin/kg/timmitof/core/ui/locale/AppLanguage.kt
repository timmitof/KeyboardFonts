package kg.timmitof.core.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/** Язык интерфейса: [tag] — BCP 47, [name] — название на самом языке, [shortName] — подпись на кнопке. */
@Immutable
data class AppLanguage(
    val tag: String,
    val name: String,
    val shortName: String,
)

/** Языки интерфейса — те же, что в каталоге клавиатуры. Порядок — порядок в списке выбора; первый — язык по умолчанию. */
val appLanguages: List<AppLanguage> = listOf(
    AppLanguage("ru", "Русский", "RU"),
    AppLanguage("en", "English", "EN"),
    AppLanguage("ky", "Кыргызча", "KY"),
    AppLanguage("kk", "Қазақша", "KK"),
    AppLanguage("uz", "Oʻzbekcha", "UZ"),
    AppLanguage("uz-Cyrl", "Ўзбекча", "ЎЗ"),
    AppLanguage("tg", "Тоҷикӣ", "TG"),
    AppLanguage("uk", "Українська", "UK"),
    AppLanguage("tr", "Türkçe", "TR"),
)

/** Язык интерфейса по языку и скрипту локали; неизвестный — язык по умолчанию. */
fun resolveAppLanguage(language: String?, script: String?): AppLanguage {
    language ?: return appLanguages.first()
    // Узбекский без явной кириллицы — латиница: она официальная.
    if (language == "uz" && script == "Cyrl") return appLanguages.first { it.tag == "uz-Cyrl" }
    return appLanguages.firstOrNull { it.tag == language } ?: appLanguages.first()
}

/** Текущий язык интерфейса и его смена. */
@Stable
interface AppLanguageController {

    val current: AppLanguage

    /** `true` — язык выбирают в самом приложении; иначе — в системных настройках приложения (iOS). */
    val isInAppSelection: Boolean

    fun select(language: AppLanguage)

    fun openSystemSettings()
}

@Composable
expect fun rememberAppLanguageController(): AppLanguageController
