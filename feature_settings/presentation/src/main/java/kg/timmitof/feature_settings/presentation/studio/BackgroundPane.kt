package kg.timmitof.feature_settings.presentation.studio

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kg.timmitof.core.ui.components.settings.SettingsSectionFooter
import kg.timmitof.core.ui.components.settings.SettingsSectionHeader
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.feature_settings.presentation.R
import kg.timmitof.feature_settings.presentation.components.BackgroundAction
import kg.timmitof.feature_settings.presentation.components.BackgroundActions
import kg.timmitof.feature_settings.presentation.components.BackgroundColorSheet
import kg.timmitof.feature_settings.presentation.components.BackgroundTile
import kg.timmitof.feature_settings.presentation.components.BackgroundTiles
import kg.timmitof.keyboard.domain.model.BackgroundPattern
import kg.timmitof.keyboard.domain.model.BackgroundPhoto
import kg.timmitof.keyboard.domain.model.KeyColorTarget
import kg.timmitof.keyboard.domain.model.KeyboardBackground
import kg.timmitof.keyboard.domain.model.KeyboardSettings

@Composable
internal fun BackgroundPane(
    settings: KeyboardSettings,
    photos: List<BackgroundPhoto>,
    onSelect: (KeyboardBackground) -> Unit,
    onPhotoPicked: (String) -> Unit,
    onEditPhoto: (Long) -> Unit,
    onDraftPreview: (KeyboardBackground.Solid?) -> Unit,
    onKeyColor: (KeyColorTarget, Long?) -> Unit,
) {
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onPhotoPicked(it.toString()) }
    }
    val pickPhoto = remember {
        { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    }
    var isColorSheetOpen by rememberSaveable { mutableStateOf(false) }
    val openColorSheet = remember { { isColorSheetOpen = true } }

    // «Нарисовать» появится здесь вместе с редактором фона.
    val galleryLabel = stringResource(R.string.background_from_gallery)
    val galleryIcon = painterResource(R.drawable.ic_background_gallery)
    val colorLabel = stringResource(R.string.background_color)
    val colorIcon = painterResource(R.drawable.ic_background_color)
    val actions = remember(galleryLabel, galleryIcon, colorLabel, colorIcon) {
        listOf(
            BackgroundAction(
                label = galleryLabel,
                icon = galleryIcon,
                role = AccentRole.APPEARANCE,
                onClick = pickPhoto,
            ),
            BackgroundAction(
                label = colorLabel,
                icon = colorIcon,
                role = AccentRole.SUCCESS,
                onClick = openColorSheet,
            ),
        )
    }
    val noneLabel = stringResource(R.string.background_none)
    val patternLabels = BackgroundPattern.entries.map { stringResource(it.labelRes) }
    val tiles = remember(noneLabel, patternLabels) {
        listOf(BackgroundTile(noneLabel, KeyboardBackground.None)) +
            BackgroundPattern.entries.mapIndexed { index, pattern ->
                BackgroundTile(patternLabels[index], KeyboardBackground.Pattern(pattern))
            }
    }
    val addPhotoLabel = stringResource(R.string.background_add_photo)
    val photoTiles = remember(addPhotoLabel, photos) {
        listOf(BackgroundTile(addPhotoLabel, null)) +
            photos.map { BackgroundTile(label = null, background = KeyboardBackground.Photo(it)) }
    }
    val selectedPhotoId = (settings.background as? KeyboardBackground.Photo)?.photo?.id

    BackgroundActions(actions = actions)

    Column {
        SettingsSectionHeader(title = stringResource(R.string.background_presets_header))
        BackgroundTiles(
            tiles = tiles,
            selected = settings.background,
            onClick = { tile -> tile.background?.let(onSelect) },
        )
    }

    Column {
        SettingsSectionHeader(title = stringResource(R.string.background_my_photos))
        BackgroundTiles(
            tiles = photoTiles,
            selected = settings.background,
            onClick = { tile ->
                val photo = (tile.background as? KeyboardBackground.Photo)?.photo
                when {
                    photo == null -> pickPhoto()
                    photo.id == selectedPhotoId -> onEditPhoto(photo.id)
                    else -> onSelect(KeyboardBackground.Photo(photo))
                }
            },
        )
        if (photos.isNotEmpty()) {
            SettingsSectionFooter(text = stringResource(R.string.background_photos_hint))
        }
        if (settings.background != KeyboardBackground.None) {
            SettingsSectionFooter(text = stringResource(R.string.background_colors_hint))
        }
    }

    KeyColorsSection(settings = settings, onKeyColor = onKeyColor)

    if (isColorSheetOpen) {
        // Шторка открывается с текущим цветом фона, а если фон не цветной — с первого из готовых.
        val initial = remember {
            Color(((settings.background as? KeyboardBackground.Solid)?.argb ?: DefaultBackgroundColor).toInt())
        }
        BackgroundColorSheet(
            initial = initial,
            title = stringResource(R.string.background_color_title),
            toneLabel = stringResource(R.string.background_custom_tone),
            cancelLabel = stringResource(R.string.background_cancel),
            applyLabel = stringResource(R.string.background_apply),
            onPreview = { color -> onDraftPreview(KeyboardBackground.Solid(color.toArgbLong())) },
            onApply = { color ->
                onSelect(KeyboardBackground.Solid(color.toArgbLong()))
                isColorSheetOpen = false
            },
            onDismiss = {
                isColorSheetOpen = false
                onDraftPreview(null)
            },
        )
    }
}

private fun Color.toArgbLong(): Long = toArgb().toLong() and 0xFFFFFFFFL

private const val DefaultBackgroundColor = 0xFFCDEFE7L

private val BackgroundPattern.labelRes: Int
    get() = when (this) {
        BackgroundPattern.MINT -> R.string.background_mint
        BackgroundPattern.STRIPES -> R.string.background_stripes
        BackgroundPattern.BUBBLES -> R.string.background_bubbles
        BackgroundPattern.GRID -> R.string.background_grid
        BackgroundPattern.WAVES -> R.string.background_waves
        BackgroundPattern.NIGHT -> R.string.background_night
    }
