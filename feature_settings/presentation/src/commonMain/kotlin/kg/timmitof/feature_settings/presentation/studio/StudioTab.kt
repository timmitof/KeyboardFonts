package kg.timmitof.feature_settings.presentation.studio

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
import kg.timmitof.feature_settings.presentation.resources.Res
import kg.timmitof.feature_settings.presentation.resources.ic_tab_background
import kg.timmitof.feature_settings.presentation.resources.ic_tab_clipboard
import kg.timmitof.feature_settings.presentation.resources.ic_tab_fonts
import kg.timmitof.feature_settings.presentation.resources.ic_tab_input
import kg.timmitof.feature_settings.presentation.resources.ic_tab_languages
import kg.timmitof.feature_settings.presentation.resources.ic_tab_size
import kg.timmitof.feature_settings.presentation.resources.ic_tab_sound
import kg.timmitof.feature_settings.presentation.resources.ic_tab_theme
import org.jetbrains.compose.resources.DrawableResource

/** Порядок записей — порядок чипов; чип появляется, только когда вкладке дали содержимое в [StudioScope.tab]. */
enum class StudioTab(
    val labelRes: StringResource,
    val iconRes: DrawableResource,
    val role: AccentRole,
) {
    BACKGROUND(MR.strings.studio_tab_background, Res.drawable.ic_tab_background, AccentRole.APPEARANCE),
    THEME(MR.strings.studio_tab_theme, Res.drawable.ic_tab_theme, AccentRole.APPEARANCE),
    FONTS(MR.strings.studio_tab_fonts, Res.drawable.ic_tab_fonts, AccentRole.BRAND),
    INPUT(MR.strings.studio_tab_input, Res.drawable.ic_tab_input, AccentRole.SUCCESS),
    LANGUAGES(MR.strings.studio_tab_languages, Res.drawable.ic_tab_languages, AccentRole.HINT),
    SIZE(MR.strings.studio_tab_size, Res.drawable.ic_tab_size, AccentRole.HINT),
    SOUND(MR.strings.studio_tab_sound, Res.drawable.ic_tab_sound, AccentRole.SUCCESS),
    CLIPBOARD(MR.strings.studio_tab_clipboard, Res.drawable.ic_tab_clipboard, AccentRole.SUCCESS);

    companion object {
        val Default = BACKGROUND
    }
}
