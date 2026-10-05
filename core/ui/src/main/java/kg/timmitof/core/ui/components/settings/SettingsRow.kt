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
    ) : SettingsRow {
        /** Лямбды в сравнении не участвуют: строка пересобирается только при смене данных. */
        override fun equals(other: Any?): Boolean =
            this === other || (other is Toggle &&
                title == other.title &&
                description == other.description &&
                icon == other.icon &&
                isNested == other.isNested &&
                isEnabled == other.isEnabled &&
                checked == other.checked)

        override fun hashCode(): Int {
            var result = title.hashCode()
            result = 31 * result + (description?.hashCode() ?: 0)
            result = 31 * result + (icon?.hashCode() ?: 0)
            result = 31 * result + isNested.hashCode()
            result = 31 * result + isEnabled.hashCode()
            result = 31 * result + checked.hashCode()
            return result
        }
    }

    @Immutable
    data class Navigation(
        override val title: String,
        override val description: String? = null,
        override val icon: Painter? = null,
        override val isNested: Boolean = false,
        override val isEnabled: Boolean = true,
        val value: String? = null,
        val onClick: () -> Unit,
    ) : SettingsRow {
        /** Лямбды в сравнении не участвуют: строка пересобирается только при смене данных. */
        override fun equals(other: Any?): Boolean =
            this === other || (other is Navigation &&
                title == other.title &&
                description == other.description &&
                icon == other.icon &&
                isNested == other.isNested &&
                isEnabled == other.isEnabled &&
                value == other.value)

        override fun hashCode(): Int {
            var result = title.hashCode()
            result = 31 * result + (description?.hashCode() ?: 0)
            result = 31 * result + (icon?.hashCode() ?: 0)
            result = 31 * result + isNested.hashCode()
            result = 31 * result + isEnabled.hashCode()
            result = 31 * result + (value?.hashCode() ?: 0)
            return result
        }
    }

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
    ) : SettingsRow {
        /** Лямбды в сравнении не участвуют: строка пересобирается только при смене данных. */
        override fun equals(other: Any?): Boolean =
            this === other || (other is Segmented &&
                title == other.title &&
                description == other.description &&
                icon == other.icon &&
                isNested == other.isNested &&
                isEnabled == other.isEnabled &&
                options == other.options &&
                selectedIndex == other.selectedIndex)

        override fun hashCode(): Int {
            var result = title.hashCode()
            result = 31 * result + (description?.hashCode() ?: 0)
            result = 31 * result + (icon?.hashCode() ?: 0)
            result = 31 * result + isNested.hashCode()
            result = 31 * result + isEnabled.hashCode()
            result = 31 * result + options.hashCode()
            result = 31 * result + selectedIndex.hashCode()
            return result
        }
    }

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
        val onValueChangeFinished: (() -> Unit)? = null,
    ) : SettingsRow {
        /** Лямбды в сравнении не участвуют: строка пересобирается только при смене данных. */
        override fun equals(other: Any?): Boolean =
            this === other || (other is Slider &&
                title == other.title &&
                description == other.description &&
                icon == other.icon &&
                isNested == other.isNested &&
                isEnabled == other.isEnabled &&
                value == other.value &&
                valueRange == other.valueRange &&
                steps == other.steps &&
                valueLabel == other.valueLabel)

        override fun hashCode(): Int {
            var result = title.hashCode()
            result = 31 * result + (description?.hashCode() ?: 0)
            result = 31 * result + (icon?.hashCode() ?: 0)
            result = 31 * result + isNested.hashCode()
            result = 31 * result + isEnabled.hashCode()
            result = 31 * result + (value?.hashCode() ?: 0)
            result = 31 * result + valueRange.hashCode()
            result = 31 * result + steps.hashCode()
            result = 31 * result + (valueLabel?.hashCode() ?: 0)
            return result
        }
    }

    /** [onPickCustom] = `null` — только готовые цвета, без своего; [autoColor] — кружок «Авто» первым, выбран при [selected] = `null`. */
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
        val autoColor: Color? = null,
        val autoLabel: String = "",
        val onAuto: () -> Unit = {},
    ) : SettingsRow {
        /** Лямбды в сравнении не участвуют: строка пересобирается только при смене данных. */
        override fun equals(other: Any?): Boolean =
            this === other || (other is Colors &&
                title == other.title &&
                description == other.description &&
                icon == other.icon &&
                isNested == other.isNested &&
                isEnabled == other.isEnabled &&
                colors == other.colors &&
                selected == other.selected &&
                autoColor == other.autoColor &&
                autoLabel == other.autoLabel &&
                (onPickCustom == null) == (other.onPickCustom == null))

        override fun hashCode(): Int {
            var result = title.hashCode()
            result = 31 * result + (description?.hashCode() ?: 0)
            result = 31 * result + (icon?.hashCode() ?: 0)
            result = 31 * result + isNested.hashCode()
            result = 31 * result + isEnabled.hashCode()
            result = 31 * result + colors.hashCode()
            result = 31 * result + (selected?.hashCode() ?: 0)
            result = 31 * result + (autoColor?.hashCode() ?: 0)
            result = 31 * result + autoLabel.hashCode()
            return result
        }
    }

    /** Строка-справка без контрола; [badge] — метка слева, например код языка «RU». */
    @Immutable
    data class Info(
        override val title: String,
        override val description: String? = null,
        override val icon: Painter? = null,
        override val isNested: Boolean = false,
        override val isEnabled: Boolean = true,
        val badge: String? = null,
    ) : SettingsRow {
        /** Лямбды в сравнении не участвуют: строка пересобирается только при смене данных. */
        override fun equals(other: Any?): Boolean =
            this === other || (other is Info &&
                title == other.title &&
                description == other.description &&
                icon == other.icon &&
                isNested == other.isNested &&
                isEnabled == other.isEnabled &&
                badge == other.badge)

        override fun hashCode(): Int {
            var result = title.hashCode()
            result = 31 * result + (description?.hashCode() ?: 0)
            result = 31 * result + (icon?.hashCode() ?: 0)
            result = 31 * result + isNested.hashCode()
            result = 31 * result + isEnabled.hashCode()
            result = 31 * result + (badge?.hashCode() ?: 0)
            return result
        }
    }

    /** Функция в разработке: вместо контрола — плашка. */
    @Immutable
    data class Soon(
        override val title: String,
        override val description: String? = null,
        override val icon: Painter? = null,
        override val isNested: Boolean = false,
        val badge: String,
    ) : SettingsRow {
        /** Лямбды в сравнении не участвуют: строка пересобирается только при смене данных. */
        override fun equals(other: Any?): Boolean =
            this === other || (other is Soon &&
                title == other.title &&
                description == other.description &&
                icon == other.icon &&
                isNested == other.isNested &&
                badge == other.badge)

        override fun hashCode(): Int {
            var result = title.hashCode()
            result = 31 * result + (description?.hashCode() ?: 0)
            result = 31 * result + (icon?.hashCode() ?: 0)
            result = 31 * result + isNested.hashCode()
            result = 31 * result + badge.hashCode()
            return result
        }

        override val isEnabled: Boolean get() = false
    }
}
