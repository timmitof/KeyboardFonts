package kg.timmitof.feature_settings.presentation.studio

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kg.timmitof.core.ui.components.tabs.ChipTab
import kg.timmitof.core.ui.components.tabs.ChipTabs

/** Ограничивает область видимости DSL «Студии». */
@DslMarker
annotation class StudioDsl

/**
 * Сборщик «Студии»: вкладка = запись [StudioTab] + её содержимое.
 *
 * Сборщик намеренно не композабельный — он только запоминает, какие вкладки
 * готовы и что в них рисовать. Чипы строятся по зарегистрированным вкладкам
 * в порядке [StudioTab], поэтому незарегистрированная вкладка на экран не попадает.
 *
 * ```
 * StudioTabs(selected, onSelect) {
 *     tab(StudioTab.THEME) { ThemePane(…) }
 *     tab(StudioTab.FONTS) { FontsPane(…) }
 *     // tab(StudioTab.BACKGROUND) { BackgroundPane(…) } — когда появится редактор фона
 * }
 * ```
 */
@StudioDsl
class StudioScope internal constructor() {

    private val panes = mutableMapOf<StudioTab, @Composable ColumnScope.() -> Unit>()

    internal fun panes(): Map<StudioTab, @Composable ColumnScope.() -> Unit> = panes

    /** Зарегистрировать вкладку и её содержимое. */
    fun tab(tab: StudioTab, content: @Composable ColumnScope.() -> Unit) {
        panes[tab] = content
    }
}

/**
 * Ряд вкладок и содержимое выбранной: «как в фоторедакторе» — под закреплённым
 * предпросмотром меняется только содержимое вкладки.
 *
 * Если выбранная вкладка не зарегистрирована (её убрали из сборщика), показывается первая.
 */
@Composable
fun StudioTabs(
    selected: StudioTab,
    onSelect: (StudioTab) -> Unit,
    modifier: Modifier = Modifier,
    content: StudioScope.() -> Unit,
) {
    val panes = StudioScope().apply(content).panes()
    if (panes.isEmpty()) return

    val available = StudioTab.entries.filter { it in panes }
    val current = selected.takeIf { it in panes } ?: available.first()

    val chips = available.map { tab ->
        ChipTab(
            key = tab,
            label = stringResource(tab.labelRes),
            icon = painterResource(tab.iconRes),
            role = tab.role,
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        ChipTabs(
            tabs = chips,
            selected = current,
            onSelect = onSelect,
        )

        AnimatedContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            targetState = current,
            contentAlignment = Alignment.TopCenter,
            transitionSpec = {
                // Содержимое въезжает со стороны выбранного чипа.
                val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                (fadeIn(spring(stiffness = 500f)) +
                    slideInHorizontally(spring(dampingRatio = 0.9f, stiffness = 500f)) { it / 8 * direction })
                    .togetherWith(fadeOut(spring(stiffness = 1200f)))
            },
            label = "studioPane",
        ) { tab ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                panes[tab]?.invoke(this)
            }
        }
    }
}
