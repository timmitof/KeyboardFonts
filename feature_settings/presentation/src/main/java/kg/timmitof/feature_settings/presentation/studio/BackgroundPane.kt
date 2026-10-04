package kg.timmitof.feature_settings.presentation.studio

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kg.timmitof.core.ui.components.settings.SettingsSectionHeader
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.feature_settings.presentation.R
import kg.timmitof.feature_settings.presentation.components.BackgroundAction
import kg.timmitof.feature_settings.presentation.components.BackgroundActions
import kg.timmitof.feature_settings.presentation.components.BackgroundColorSheet
import kg.timmitof.feature_settings.presentation.components.BackgroundTile
import kg.timmitof.feature_settings.presentation.components.BackgroundTiles
import kg.timmitof.keyboard.domain.model.BackgroundPattern
import kg.timmitof.keyboard.domain.model.KeyboardBackground
import kg.timmitof.keyboard.domain.model.KeyboardSettings

@Composable
internal fun BackgroundPane(
    settings: KeyboardSettings,
    draft: KeyboardBackground.Solid?,
    onSelect: (KeyboardBackground) -> Unit,
    onPhotoPicked: (String) -> Unit,
    onColorClick: () -> Unit,
    onDraftChange: (Long) -> Unit,
    onDraftApply: () -> Unit,
    onDraftDismiss: () -> Unit,
) {
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onPhotoPicked(it.toString()) }
    }
    val pickPhoto = {
        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // «Нарисовать» появится здесь вместе с редактором фона.
    val actions = listOf(
        BackgroundAction(
            label = stringResource(R.string.background_from_gallery),
            icon = painterResource(R.drawable.ic_background_gallery),
            role = AccentRole.APPEARANCE,
            onClick = pickPhoto,
        ),
        BackgroundAction(
            label = stringResource(R.string.background_color),
            icon = painterResource(R.drawable.ic_background_color),
            role = AccentRole.SUCCESS,
            onClick = onColorClick,
        ),
    )
    val tiles = listOf(BackgroundTile(stringResource(R.string.background_none), KeyboardBackground.None)) +
        BackgroundPattern.entries.map { BackgroundTile(stringResource(it.labelRes), KeyboardBackground.Pattern(it)) } +
        BackgroundTile(stringResource(R.string.background_photo), settings.backgroundPhoto?.let(KeyboardBackground::Photo))

    BackgroundActions(actions = actions)

    Column {
        SettingsSectionHeader(title = stringResource(R.string.background_presets_header))
        BackgroundTiles(
            tiles = tiles,
            selected = settings.background,
            onClick = { tile -> tile.background?.let(onSelect) ?: pickPhoto() },
        )
    }

    draft?.let {
        BackgroundColorSheet(
            color = Color(it.argb.toInt()),
            title = stringResource(R.string.background_color_title),
            toneLabel = stringResource(R.string.background_custom_tone),
            cancelLabel = stringResource(R.string.background_cancel),
            applyLabel = stringResource(R.string.background_apply),
            onChange = { color -> onDraftChange(color.toArgb().toLong() and 0xFFFFFFFFL) },
            onApply = onDraftApply,
            onDismiss = onDraftDismiss,
        )
    }
}

private val BackgroundPattern.labelRes: Int
    get() = when (this) {
        BackgroundPattern.MINT -> R.string.background_mint
        BackgroundPattern.STRIPES -> R.string.background_stripes
        BackgroundPattern.BUBBLES -> R.string.background_bubbles
        BackgroundPattern.GRID -> R.string.background_grid
        BackgroundPattern.WAVES -> R.string.background_waves
        BackgroundPattern.NIGHT -> R.string.background_night
    }
