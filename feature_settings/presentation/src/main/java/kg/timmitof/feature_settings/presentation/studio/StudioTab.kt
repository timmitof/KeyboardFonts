package kg.timmitof.feature_settings.presentation.studio

import androidx.annotation.DrawableRes
import dev.icerock.moko.resources.StringResource
import kg.timmitof.core.resources.MR
import kg.timmitof.core.resources.studio_tab_background
import kg.timmitof.core.resources.studio_tab_clipboard
import kg.timmitof.core.resources.studio_tab_fonts
import kg.timmitof.core.resources.studio_tab_input
import kg.timmitof.core.resources.studio_tab_languages
import kg.timmitof.core.resources.studio_tab_size
import kg.timmitof.core.resources.studio_tab_sound
import kg.timmitof.core.resources.studio_tab_theme
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.feature_settings.presentation.R

/** Порядок записей — порядок чипов; чип появляется, только когда вкладке дали содержимое в [StudioScope.tab]. */
enum class StudioTab(
    val labelRes: StringResource,
    @param:DrawableRes val iconRes: Int,
    val role: AccentRole,
) {
    BACKGROUND(MR.strings.studio_tab_background, R.drawable.ic_tab_background, AccentRole.APPEARANCE),
    THEME(MR.strings.studio_tab_theme, R.drawable.ic_tab_theme, AccentRole.APPEARANCE),
    FONTS(MR.strings.studio_tab_fonts, R.drawable.ic_tab_fonts, AccentRole.BRAND),
    INPUT(MR.strings.studio_tab_input, R.drawable.ic_tab_input, AccentRole.SUCCESS),
    LANGUAGES(MR.strings.studio_tab_languages, R.drawable.ic_tab_languages, AccentRole.HINT),
    SIZE(MR.strings.studio_tab_size, R.drawable.ic_tab_size, AccentRole.HINT),
    SOUND(MR.strings.studio_tab_sound, R.drawable.ic_tab_sound, AccentRole.SUCCESS),
    CLIPBOARD(MR.strings.studio_tab_clipboard, R.drawable.ic_tab_clipboard, AccentRole.SUCCESS);

    companion object {
        val Default = BACKGROUND
    }
}
