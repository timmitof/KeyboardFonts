package kg.timmitof.feature_settings.presentation.studio

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.feature_settings.presentation.R

/** Порядок записей — порядок чипов; чип появляется, только когда вкладке дали содержимое в [StudioScope.tab]. */
enum class StudioTab(
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
    val role: AccentRole,
) {
    BACKGROUND(R.string.studio_tab_background, R.drawable.ic_tab_background, AccentRole.APPEARANCE),
    THEME(R.string.studio_tab_theme, R.drawable.ic_tab_theme, AccentRole.APPEARANCE),
    FONTS(R.string.studio_tab_fonts, R.drawable.ic_tab_fonts, AccentRole.BRAND),
    INPUT(R.string.studio_tab_input, R.drawable.ic_tab_input, AccentRole.SUCCESS),
    LANGUAGES(R.string.studio_tab_languages, R.drawable.ic_tab_languages, AccentRole.HINT),
    SIZE(R.string.studio_tab_size, R.drawable.ic_tab_size, AccentRole.HINT),
    SOUND(R.string.studio_tab_sound, R.drawable.ic_tab_sound, AccentRole.SUCCESS),
    CLIPBOARD(R.string.studio_tab_clipboard, R.drawable.ic_tab_clipboard, AccentRole.SUCCESS);

    companion object {
        val Default = BACKGROUND
    }
}
