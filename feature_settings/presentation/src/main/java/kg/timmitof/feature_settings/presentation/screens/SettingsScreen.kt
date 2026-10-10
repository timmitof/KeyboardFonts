package kg.timmitof.feature_settings.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
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
import kg.timmitof.core.ui.components.brand.AppLanguageButton
import kg.timmitof.core.ui.components.brand.BrandHeader
import kg.timmitof.core.ui.components.brand.StatusPill
import kg.timmitof.core.ui.components.hint.TipsCard
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.core.ui.theme.KeyboardFontsTheme
import kotlinx.coroutines.flow.drop
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
import kg.timmitof.keyboard.domain.model.KeyColorTarget
import kg.timmitof.keyboard.domain.model.KeyboardBackground
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardSoundPack
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.font.domain.model.FontScript
import kg.timmitof.keyboard.presentation.components.fontScript

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
    val onKeyColor = remember<(KeyColorTarget, Long?) -> Unit> {
        { target, argb -> sendEvent(SettingsEvent.KeyColorChanged(target, argb)) }
    }
    val onPanelFonts = remember<(List<String>) -> Unit> {
        { ids -> sendEvent(SettingsEvent.PanelFontsChanged(ids)) }
    }
    val onResetFonts = remember { { sendEvent(SettingsEvent.ResetFontPanelClicked) } }
    val onEnabledLanguages = remember<(List<String>) -> Unit> {
        { codes -> sendEvent(SettingsEvent.EnabledLanguagesChanged(codes)) }
    }
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
    val onEditPhoto = remember<(Long) -> Unit> {
        { id -> sendEvent(SettingsEvent.EditPhotoClicked(id)) }
    }
    val onTab = remember<(StudioTab) -> Unit> {
        { tab -> sendEvent(SettingsEvent.TabSelected(tab)) }
    }
    val onClearClipboard = remember { { sendEvent(SettingsEvent.ClearRecentClipboardClicked) } }
    val onConnect = remember { { sendEvent(SettingsEvent.ConnectKeyboardClicked) } }

    // Каждая панель читает только свой кусок состояния: derivedStateOf не будит остальных при чужих изменениях.
    val settings = remember(state) { derivedStateOf { state.value.settings } }
    val languages = remember(state) { derivedStateOf { state.value.languages } }
    val fontPanel = remember(state) { derivedStateOf { state.value.fontPanel } }
    val fontScript = remember(state) { derivedStateOf { state.value.summary.selectedLanguage.fontScript } }
    val photos = remember(state) { derivedStateOf { state.value.photos } }
    val clipboard = remember(state) { derivedStateOf { state.value.clipboard } }
    val isKeyboardReady by remember(state) { derivedStateOf { state.value.summary.isKeyboardReady } }
    val selectedTab = remember<() -> StudioTab>(state) { { state.value.selectedTab } }

    // Черновик цвета фона живёт только здесь: превью клавиатуры видит его сразу, во ViewModel он уходит по «Применить».
    val backgroundDraft = remember { mutableStateOf<KeyboardBackground.Solid?>(null) }
    val onDraftPreview = remember<(KeyboardBackground.Solid?) -> Unit> { { draft -> backgroundDraft.value = draft } }
    LaunchedEffect(settings) {
        snapshotFlow { settings.value.background }
            .drop(1)
            .collect { backgroundDraft.value = null }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

        StudioHeader(
            isKeyboardReady = isKeyboardReady,
            onConnect = onConnect,
        )

        StudioPreview(
            state = state,
            draft = backgroundDraft,
            modifier = Modifier.padding(horizontal = HorizontalPadding),
        )

        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(top = 14.dp, bottom = BottomPadding)
        ) {
            StudioTabs(
                selected = selectedTab,
                onSelect = onTab,
            ) {
                tab(StudioTab.BACKGROUND) {
                    BackgroundPane(
                        settings = settings.value,
                        photos = photos.value,
                        onSelect = onBackground,
                        onPhotoPicked = onBackgroundPhoto,
                        onEditPhoto = onEditPhoto,
                        onDraftPreview = onDraftPreview,
                        onKeyColor = onKeyColor,
                    )
                }
                tab(StudioTab.THEME) {
                    ThemePane(
                        settings = settings.value,
                        onTheme = onTheme,
                        onKeyColor = onKeyColor,
                        onToggle = onToggle,
                    )
                }
                tab(StudioTab.FONTS) {
                    FontsPane(
                        settings = settings.value,
                        panel = fontPanel.value,
                        script = fontScript.value,
                        onToggle = onToggle,
                        onPanelFonts = onPanelFonts,
                        onReset = onResetFonts,
                    )
                }
                tab(StudioTab.INPUT) {
                    InputPane(settings = settings.value, onToggle = onToggle)
                }
                tab(StudioTab.LANGUAGES) {
                    LanguagesPane(
                        languages = languages.value,
                        onEnabledLanguages = onEnabledLanguages,
                    )
                }
                tab(StudioTab.SIZE) {
                    SizePane(settings = settings.value, onHeight = onHeight, onToggle = onToggle)
                }
                tab(StudioTab.SOUND) {
                    SoundPane(
                        settings = settings.value,
                        onToggle = onToggle,
                        onSoundPack = onSoundPack,
                        onSoundVolume = onSoundVolume,
                    )
                }
                tab(StudioTab.CLIPBOARD) {
                    ClipboardPane(board = clipboard.value, onClearRecent = onClearClipboard)
                }
            }

            StudioTips(modifier = Modifier.padding(horizontal = HorizontalPadding, vertical = 12.dp))

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
        }
    }
}

/** Читает состояние сам, чтобы рекомпозиция превью не задевала остальной экран. */
@Composable
private fun StudioPreview(
    state: State<SettingsState>,
    draft: State<KeyboardBackground.Solid?>,
    modifier: Modifier = Modifier,
) {
    StudioPreviewCard(
        modifier = modifier,
        settings = state.value.settings,
        draft = { draft.value },
        summary = state.value.summary,
        fonts = state.value.fontPanel.visible,
        sample = stringResource(
            if (state.value.summary.selectedLanguage.fontScript == FontScript.CYRILLIC) {
                R.string.studio_preview_sample_cyrillic
            } else {
                R.string.studio_preview_sample
            }
        ),
        checkLabel = stringResource(R.string.studio_preview_check),
    )
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
        AppLanguageButton()
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
