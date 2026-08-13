package kg.timmitof.feature_settings.presentation.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import kg.timmitof.core.ui.base.Container
import kg.timmitof.core.ui.base.ContainerDSLBuilder
import kg.timmitof.core.ui.components.AppTopBar
import kg.timmitof.core.ui.theme.KeyboardFontsTheme
import kg.timmitof.feature_settings.presentation.R
import kg.timmitof.feature_settings.presentation.components.AppSection
import kg.timmitof.feature_settings.presentation.components.FeedbackSection
import kg.timmitof.feature_settings.presentation.components.FontsSection
import kg.timmitof.feature_settings.presentation.components.KeyboardSection
import kg.timmitof.feature_settings.presentation.components.SoonSection
import kg.timmitof.feature_settings.presentation.components.TextInputSection
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle

/**
 * Настройки клавиатуры — корневой экран приложения.
 *
 * Экран собран из секций: заголовок + карточка со строками. Новая функция —
 * это новая секция, порядок остальных при этом не меняется.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Container(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        viewModel = viewModel,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings_title),
                scrollBehavior = scrollBehavior
            )
        }
    ) { state, innerPadding ->
        SettingsContent(
            state = state,
            innerPadding = innerPadding
        )
    }
}

@Composable
internal fun ContainerDSLBuilder<SettingsSideEffect, SettingsEvent>.SettingsContent(
    state: State<SettingsState>,
    innerPadding: PaddingValues = PaddingValues()
) {
    val scrollState = rememberScrollState()

    // Клавиатуру включают в системных настройках — статус перечитываем при возврате
    LifecycleResumeEffect(Unit) {
        sendEvent(SettingsEvent.ScreenResumed)
        onPauseOrDispose { }
    }

    val onToggle = remember<(KeyboardToggle, Boolean) -> Unit> {
        { toggle, enabled -> sendEvent(SettingsEvent.ToggleChanged(toggle, enabled)) }
    }
    val onTheme = remember<(KeyboardThemeMode) -> Unit> {
        { mode -> sendEvent(SettingsEvent.ThemeChanged(mode)) }
    }
    val onCheckKeyboard = remember { { sendEvent(SettingsEvent.CheckKeyboardClicked) } }

    val settings = state.value.settings
    val summary = state.value.summary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
            .padding(bottom = BottomPadding)
    ) {
        AppSection(
            isKeyboardReady = summary.isKeyboardReady,
            onCheckKeyboard = onCheckKeyboard
        )

        TextInputSection(
            settings = settings,
            onToggle = onToggle
        )

        FontsSection(
            settings = settings,
            fontsTotal = summary.fontsTotal,
            onToggle = onToggle
        )

        KeyboardSection(
            settings = settings,
            languages = summary.languages,
            onToggle = onToggle,
            onTheme = onTheme
        )

        FeedbackSection(
            settings = settings,
            onToggle = onToggle
        )

        SoonSection()
    }
}

/** Последняя карточка не должна упираться в край экрана. */
private val BottomPadding = 24.dp

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview() {
    KeyboardFontsTheme {
        Surface {
            ContainerDSLBuilder<SettingsSideEffect, SettingsEvent>({}).SettingsContent(
                state = remember { mutableStateOf(SettingsState()) }
            )
        }
    }
}
