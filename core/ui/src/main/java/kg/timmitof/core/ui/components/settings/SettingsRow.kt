package kg.timmitof.core.ui.components.settings

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

/** Строка настроек — кирпич любой секции; [icon] ставится только первой строке секции, иначе список рябит. */
@Immutable
sealed interface SettingsRow {

    val title: String
    val description: String?
    val icon: Painter?
    val isNested: Boolean
    val isEnabled: Boolean

    @Immutable
    data class Toggle(
        override val title: String,
        override val description: String? = null,
        override val icon: Painter? = null,
        override val isNested: Boolean = false,
        override val isEnabled: Boolean = true,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
    ) : SettingsRow

    @Immutable
    data class Navigation(
        override val title: String,
        override val description: String? = null,
        override val icon: Painter? = null,
        override val isNested: Boolean = false,
        override val isEnabled: Boolean = true,
        val value: String? = null,
        val onClick: () -> Unit,
    ) : SettingsRow

    @Immutable
    data class Segmented(
        override val title: String,
        override val description: String? = null,
        override val icon: Painter? = null,
        override val isNested: Boolean = false,
        override val isEnabled: Boolean = true,
        val options: List<String>,
        val selectedIndex: Int,
        val onSelect: (Int) -> Unit,
    ) : SettingsRow

    @Immutable
    data class Slider(
        override val title: String,
        override val description: String? = null,
        override val icon: Painter? = null,
        override val isNested: Boolean = false,
        override val isEnabled: Boolean = true,
        val value: Float,
        val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        val steps: Int = 0,
        val valueLabel: String? = null,
        val onValueChange: (Float) -> Unit,
    ) : SettingsRow

    /** [onPickCustom] = `null` — только готовые цвета, без своего. */
    @Immutable
    data class Colors(
        override val title: String,
        override val description: String? = null,
        override val icon: Painter? = null,
        override val isNested: Boolean = false,
        override val isEnabled: Boolean = true,
        val colors: List<Color>,
        val selected: Color?,
        val onSelect: (Color) -> Unit,
        val onPickCustom: (() -> Unit)? = null,
    ) : SettingsRow

    /** Строка-справка без контрола; [badge] — метка слева, например код языка «RU». */
    @Immutable
    data class Info(
        override val title: String,
        override val description: String? = null,
        override val icon: Painter? = null,
        override val isNested: Boolean = false,
        override val isEnabled: Boolean = true,
        val badge: String? = null,
    ) : SettingsRow

    /** Функция в разработке: вместо контрола — плашка. */
    @Immutable
    data class Soon(
        override val title: String,
        override val description: String? = null,
        override val icon: Painter? = null,
        override val isNested: Boolean = false,
        val badge: String,
    ) : SettingsRow {
        override val isEnabled: Boolean get() = false
    }
}
