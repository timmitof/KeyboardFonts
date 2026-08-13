package kg.timmitof.core.ui.components.settings

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.painter.Painter

/**
 * Строка настроек — кирпич, из которого собирается любая секция.
 *
 * Типов ровно пять: переключатель, переход, выбор из нескольких вариантов,
 * число в диапазоне и заглушка «Скоро». Новая функция описывается ими же,
 * поэтому экран настроек растёт без правок разметки.
 *
 * @property title заголовок строки.
 * @property description пояснение под заголовком — только там, где без него непонятно.
 * @property icon иконка темы; ставится **только первой строке секции**, иначе список рябит.
 * @property isNested строка зависит от переключателя выше — рисуется с отступом и линией связи.
 * @property isEnabled родитель включён; выключенная строка гаснет, но остаётся видимой.
 */
@Immutable
sealed interface SettingsRow {

    val title: String
    val description: String?
    val icon: Painter?
    val isNested: Boolean
    val isEnabled: Boolean

    /** Переключатель: единственное значение — да/нет. */
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

    /** Переход на подэкран; [value] показывает текущий выбор, не заходя внутрь. */
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

    /** Выбор из двух-трёх вариантов — сегменты помещаются в строку целиком. */
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

    /** Число в диапазоне: ползунок занимает вторую строку под заголовком. */
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

    /**
     * Функция в разработке: вместо контрола — плашка.
     *
     * Своя секция «Скоро» позволяет показать план, не перестраивая экран,
     * когда функция наконец появится.
     */
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
