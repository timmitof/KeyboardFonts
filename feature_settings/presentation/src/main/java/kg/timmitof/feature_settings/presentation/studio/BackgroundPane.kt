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
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import kg.timmitof.core.resources.MR
import kg.timmitof.core.resources.background_add_photo
import kg.timmitof.core.resources.background_apply
import kg.timmitof.core.resources.background_bubbles
import kg.timmitof.core.resources.background_cancel
import kg.timmitof.core.resources.background_color
import kg.timmitof.core.resources.background_color_title
import kg.timmitof.core.resources.background_colors_hint
import kg.timmitof.core.resources.background_custom_tone
import kg.timmitof.core.resources.background_from_gallery
import kg.timmitof.core.resources.background_grid
import kg.timmitof.core.resources.background_mint
import kg.timmitof.core.resources.background_my_photos
import kg.timmitof.core.resources.background_night
import kg.timmitof.core.resources.background_none
import kg.timmitof.core.resources.background_photos_hint
import kg.timmitof.core.resources.background_presets_header
import kg.timmitof.core.resources.background_stripes
import kg.timmitof.core.resources.background_waves
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
    val galleryLabel = stringResource(MR.strings.background_from_gallery)
    val galleryIcon = painterResource(R.drawable.ic_background_gallery)
    val colorLabel = stringResource(MR.strings.background_color)
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
    val noneLabel = stringResource(MR.strings.background_none)
    val patternLabels = BackgroundPattern.entries.map { stringResource(it.labelRes) }
    val tiles = remember(noneLabel, patternLabels) {
        listOf(BackgroundTile(noneLabel, KeyboardBackground.None)) +
            BackgroundPattern.entries.mapIndexed { index, pattern ->
                BackgroundTile(patternLabels[index], KeyboardBackground.Pattern(pattern))
            }
    }
    val addPhotoLabel = stringResource(MR.strings.background_add_photo)
    val photoTiles = remember(addPhotoLabel, photos) {
        listOf(BackgroundTile(addPhotoLabel, null)) +
            photos.map { BackgroundTile(label = null, background = KeyboardBackground.Photo(it)) }
    }
    val selectedPhotoId = (settings.background as? KeyboardBackground.Photo)?.photo?.id

    BackgroundActions(actions = actions)

    Column {
        SettingsSectionHeader(title = stringResource(MR.strings.background_presets_header))
        BackgroundTiles(
            tiles = tiles,
            selected = settings.background,
            onClick = { tile -> tile.background?.let(onSelect) },
        )
    }

    Column {
        SettingsSectionHeader(title = stringResource(MR.strings.background_my_photos))
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
            SettingsSectionFooter(text = stringResource(MR.strings.background_photos_hint))
        }
        if (settings.background != KeyboardBackground.None) {
            SettingsSectionFooter(text = stringResource(MR.strings.background_colors_hint))
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
            title = stringResource(MR.strings.background_color_title),
            toneLabel = stringResource(MR.strings.background_custom_tone),
            cancelLabel = stringResource(MR.strings.background_cancel),
            applyLabel = stringResource(MR.strings.background_apply),
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

private val BackgroundPattern.labelRes: StringResource
    get() = when (this) {
        BackgroundPattern.MINT -> MR.strings.background_mint
        BackgroundPattern.STRIPES -> MR.strings.background_stripes
        BackgroundPattern.BUBBLES -> MR.strings.background_bubbles
        BackgroundPattern.GRID -> MR.strings.background_grid
        BackgroundPattern.WAVES -> MR.strings.background_waves
        BackgroundPattern.NIGHT -> MR.strings.background_night
    }
