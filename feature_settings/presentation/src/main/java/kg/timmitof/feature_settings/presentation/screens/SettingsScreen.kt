package kg.timmitof.feature_settings.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import kg.timmitof.core.ui.base.Container
import kg.timmitof.core.ui.base.ContainerDSLBuilder
import kg.timmitof.core.ui.components.brand.BrandHeader
import kg.timmitof.core.ui.components.brand.StatusPill
import kg.timmitof.core.ui.components.hint.TipsCard
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.core.ui.theme.KeyboardFontsTheme
import kg.timmitof.feature_settings.presentation.R
import kg.timmitof.feature_settings.presentation.components.StudioPreviewCard
import kg.timmitof.feature_settings.presentation.studio.BackgroundPane
import kg.timmitof.feature_settings.presentation.studio.ClipboardPane
import kg.timmitof.feature_settings.presentation.studio.FontsPane
import kg.timmitof.feature_settings.presentation.studio.InputPane
import kg.timmitof.feature_settings.presentation.studio.LanguagesPane
import kg.timmitof.feature_settings.presentation.studio.SizePane
import kg.timmitof.feature_settings.presentation.studio.SoundPane
import kg.timmitof.feature_settings.presentation.studio.StudioTab
import kg.timmitof.feature_settings.presentation.studio.StudioTabs
import kg.timmitof.feature_settings.presentation.studio.ThemePane
import kg.timmitof.keyboard.domain.model.KeyboardBackground
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardSoundPack
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Container(
        modifier = Modifier.fillMaxSize(),
        viewModel = viewModel,
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

    // Клавиатуру включают в системных настройках, язык и шрифт меняют на ней самой —
    // всё это перечитываем при возврате.
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
    val onHeight = remember<(KeyboardHeight) -> Unit> {
        { height -> sendEvent(SettingsEvent.HeightChanged(height)) }
    }
    val onEnterColor = remember<(Long?) -> Unit> {
        { argb -> sendEvent(SettingsEvent.EnterColorChanged(argb)) }
    }
    val onPanelFonts = remember<(List<String>) -> Unit> {
        { ids -> sendEvent(SettingsEvent.PanelFontsChanged(ids)) }
    }
    val onResetFonts = remember { { sendEvent(SettingsEvent.ResetFontPanelClicked) } }
    val onSoundPack = remember<(KeyboardSoundPack) -> Unit> {
        { pack -> sendEvent(SettingsEvent.SoundPackChanged(pack)) }
    }
    val onSoundVolume = remember<(Float) -> Unit> {
        { volume -> sendEvent(SettingsEvent.SoundVolumeChanged(volume)) }
    }
    val onBackground = remember<(KeyboardBackground) -> Unit> {
        { background -> sendEvent(SettingsEvent.BackgroundSelected(background)) }
    }
    val onBackgroundPhoto = remember<(String) -> Unit> {
        { uri -> sendEvent(SettingsEvent.BackgroundPhotoPicked(uri)) }
    }
    val onBackgroundDraft = remember<(Long) -> Unit> {
        { argb -> sendEvent(SettingsEvent.BackgroundDraftChanged(argb)) }
    }
    val onBackgroundColor = remember { { sendEvent(SettingsEvent.BackgroundColorClicked) } }
    val onBackgroundDraftApply = remember { { sendEvent(SettingsEvent.BackgroundDraftApplied) } }
    val onBackgroundDraftDismiss = remember { { sendEvent(SettingsEvent.BackgroundDraftDismissed) } }
    val onTab = remember<(StudioTab) -> Unit> {
        { tab -> sendEvent(SettingsEvent.TabSelected(tab)) }
    }
    val onClearClipboard = remember { { sendEvent(SettingsEvent.ClearRecentClipboardClicked) } }
    val onConnect = remember { { sendEvent(SettingsEvent.ConnectKeyboardClicked) } }

    val settings = state.value.settings
    val summary = state.value.summary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
    ) {
        StudioHeader(
            isKeyboardReady = summary.isKeyboardReady,
            onConnect = onConnect,
        )

        StudioPreviewCard(
            modifier = Modifier.padding(horizontal = HorizontalPadding),
            settings = state.value.previewSettings,
            summary = summary,
            fonts = state.value.fontPanel.visible,
            sample = stringResource(R.string.studio_preview_sample),
            checkLabel = stringResource(R.string.studio_preview_check),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(top = 14.dp, bottom = BottomPadding)
        ) {
            StudioTabs(
                selected = state.value.selectedTab,
                onSelect = onTab,
            ) {
                tab(StudioTab.BACKGROUND) {
                    BackgroundPane(
                        settings = settings,
                        draft = state.value.backgroundDraft,
                        onSelect = onBackground,
                        onPhotoPicked = onBackgroundPhoto,
                        onColorClick = onBackgroundColor,
                        onDraftChange = onBackgroundDraft,
                        onDraftApply = onBackgroundDraftApply,
                        onDraftDismiss = onBackgroundDraftDismiss,
                    )
                }
                tab(StudioTab.THEME) {
                    ThemePane(
                        settings = settings,
                        onTheme = onTheme,
                        onEnterColor = onEnterColor,
                        onToggle = onToggle,
                    )
                }
                tab(StudioTab.FONTS) {
                    FontsPane(
                        settings = settings,
                        panel = state.value.fontPanel,
                        onToggle = onToggle,
                        onPanelFonts = onPanelFonts,
                        onReset = onResetFonts,
                    )
                }
                tab(StudioTab.INPUT) {
                    InputPane(settings = settings, onToggle = onToggle)
                }
                tab(StudioTab.LANGUAGES) {
                    LanguagesPane(languages = summary.languages, selected = summary.selectedLanguage)
                }
                tab(StudioTab.SIZE) {
                    SizePane(settings = settings, onHeight = onHeight, onToggle = onToggle)
                }
                tab(StudioTab.SOUND) {
                    SoundPane(
                        settings = settings,
                        onToggle = onToggle,
                        onSoundPack = onSoundPack,
                        onSoundVolume = onSoundVolume,
                    )
                }
                tab(StudioTab.CLIPBOARD) {
                    ClipboardPane(board = state.value.clipboard, onClearRecent = onClearClipboard)
                }
            }

            StudioTips(modifier = Modifier.padding(horizontal = HorizontalPadding, vertical = 12.dp))
        }
    }
}

@Composable
private fun StudioHeader(
    isKeyboardReady: Boolean,
    onConnect: () -> Unit,
) {
    BrandHeader(
        modifier = Modifier.padding(horizontal = HorizontalPadding),
        title = stringResource(R.string.studio_app_name),
    ) {
        if (isKeyboardReady) {
            StatusPill(
                text = stringResource(R.string.studio_status_active),
                role = AccentRole.SUCCESS,
            )
        } else {
            StatusPill(
                text = stringResource(R.string.studio_status_not_ready),
                role = AccentRole.HINT,
                onClick = onConnect,
            )
        }
    }
}

@Composable
private fun StudioTips(modifier: Modifier = Modifier) {
    val tips = StudioTipsRes.map { stringResource(it) }
    val counter = stringResource(R.string.tip_counter)

    TipsCard(
        modifier = modifier,
        tips = tips,
        counter = { index, total -> counter.format(index, total) },
    )
}

private val StudioTipsRes = listOf(
    R.string.tip_space_language,
    R.string.tip_space_cursor,
    R.string.tip_backspace_slide,
    R.string.tip_long_press,
    R.string.tip_quick_settings,
    R.string.tip_clipboard,
)

private val HorizontalPadding = 16.dp

private val BottomPadding = 12.dp

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
