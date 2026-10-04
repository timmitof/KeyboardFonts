package kg.timmitof.feature_settings.presentation.studio

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.feature_settings.presentation.R

/**
 * Вкладка «Студии». Порядок записей — порядок чипов: самые частые стоят первыми.
 *
 * Запись здесь ещё не выводит вкладку на экран — чип появляется, только когда
 * вкладке дали содержимое в [StudioScope.tab]. Так готовые к работе разделы
 * (переводчик, голосовой ввод) добавляются одной записью и одной строкой в сборщике.
 *
 * @property role цвет кружка иконки: внешний вид — розовый, шрифты — фиолетовый,
 * ввод и отклик — бирюзовый, языки и размер — янтарный.
 */
enum class StudioTab(
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
    val role: AccentRole,
) {
    /** Свой фон клавиатуры: галерея, рисунок, цвет. Ждёт редактора фона. */
    BACKGROUND(R.string.studio_tab_background, R.drawable.ic_tab_background, AccentRole.APPEARANCE),
    THEME(R.string.studio_tab_theme, R.drawable.ic_tab_theme, AccentRole.APPEARANCE),
    FONTS(R.string.studio_tab_fonts, R.drawable.ic_tab_fonts, AccentRole.BRAND),
    INPUT(R.string.studio_tab_input, R.drawable.ic_tab_input, AccentRole.SUCCESS),
    LANGUAGES(R.string.studio_tab_languages, R.drawable.ic_tab_languages, AccentRole.HINT),
    SIZE(R.string.studio_tab_size, R.drawable.ic_tab_size, AccentRole.HINT),
    SOUND(R.string.studio_tab_sound, R.drawable.ic_tab_sound, AccentRole.SUCCESS),
    CLIPBOARD(R.string.studio_tab_clipboard, R.drawable.ic_tab_clipboard, AccentRole.SUCCESS);

    companion object {
        /** Вкладка при первом открытии. */
        val Default = THEME
    }
}
