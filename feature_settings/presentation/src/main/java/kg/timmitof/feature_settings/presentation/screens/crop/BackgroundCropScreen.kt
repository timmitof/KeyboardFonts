package kg.timmitof.feature_settings.presentation.screens.crop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kg.timmitof.core.ui.base.Container
import kg.timmitof.core.ui.base.ContainerDSLBuilder
import kg.timmitof.core.ui.components.AppTopBar
import kg.timmitof.feature_settings.presentation.R
import kg.timmitof.feature_settings.presentation.components.PhotoCropFrame
import kg.timmitof.feature_settings.presentation.components.PhotoCropState
import kg.timmitof.keyboard.domain.model.KeyboardBackground
import kg.timmitof.keyboard.presentation.preview.keyboardHeight
import kg.timmitof.keyboard.presentation.theme.appearance
import kg.timmitof.keyboard.presentation.theme.colorScheme

@Composable
fun BackgroundCropScreen(
    viewModel: BackgroundCropViewModel = hiltViewModel()
) {
    Container(
        modifier = Modifier.fillMaxSize(),
        viewModel = viewModel,
    ) { state, innerPadding ->
        BackgroundCropContent(
            state = state,
            innerPadding = innerPadding,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContainerDSLBuilder<BackgroundCropSideEffect, BackgroundCropEvent>.BackgroundCropContent(
    state: State<BackgroundCropState>,
    innerPadding: PaddingValues = PaddingValues(),
) {
    onBack { sendEvent(BackgroundCropEvent.BackClicked) }

    val cropState = remember { PhotoCropState() }
    var isDeleteAsked by rememberSaveable { mutableStateOf(false) }
    val photo = state.value.photo

    val settings = state.value.settings
    val isSystemDark = isSystemInDarkTheme()
    // Клавиши поверх фото — цвета темы: свои цвета под фото подберутся после сохранения.
    val keyColors = remember(settings, isSystemDark) {
        settings.copy(background = KeyboardBackground.None).appearance(isSystemDark).colorScheme()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding(),
            ),
    ) {
        AppTopBar(
            title = stringResource(R.string.background_crop_title),
            onBack = { sendEvent(BackgroundCropEvent.BackClicked) },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = HorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.background_crop_hint),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            photo?.let {
                PhotoCropFrame(
                    path = it.path,
                    initialCrop = it.crop,
                    state = cropState,
                    keyboardHeight = settings.keyboardHeight(),
                    screenWidth = LocalConfiguration.current.screenWidthDp.dp,
                    keyColors = keyColors,
                    errorText = stringResource(R.string.background_crop_error),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalButton(
                modifier = Modifier
                    .weight(1f)
                    .height(ButtonHeight),
                enabled = !state.value.isSaving && photo != null,
                onClick = { isDeleteAsked = true },
            ) {
                Text(text = stringResource(R.string.background_crop_delete))
            }
            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(ButtonHeight),
                enabled = !state.value.isSaving && photo != null,
                onClick = { cropState.crop()?.let { sendEvent(BackgroundCropEvent.DoneClicked(it)) } },
            ) {
                if (state.value.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(text = stringResource(R.string.background_crop_done))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }

    if (isDeleteAsked) {
        AlertDialog(
            onDismissRequest = { isDeleteAsked = false },
            text = { Text(text = stringResource(R.string.background_crop_delete_question)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleteAsked = false
                        sendEvent(BackgroundCropEvent.DeleteClicked)
                    },
                ) {
                    Text(text = stringResource(R.string.background_crop_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { isDeleteAsked = false }) {
                    Text(text = stringResource(R.string.color_picker_dismiss))
                }
            },
        )
    }
}

private val HorizontalPadding = 16.dp
private val ButtonHeight = 52.dp
