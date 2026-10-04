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

@DslMarker
annotation class StudioDsl

/** Сборщик не композабельный: чипы строятся по зарегистрированным вкладкам в порядке [StudioTab]. */
@StudioDsl
class StudioScope internal constructor() {

    private val panes = mutableMapOf<StudioTab, @Composable ColumnScope.() -> Unit>()

    internal fun panes(): Map<StudioTab, @Composable ColumnScope.() -> Unit> = panes

    fun tab(tab: StudioTab, content: @Composable ColumnScope.() -> Unit) {
        panes[tab] = content
    }
}

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
