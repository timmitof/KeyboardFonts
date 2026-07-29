package kg.timmitof.feature_home.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kg.timmitof.core.ui.theme.KeyboardFontsTheme
import kg.timmitof.feature_home.domain.model.KeyboardSetupModel
import kg.timmitof.feature_home.domain.model.KeyboardSetupStep
import kg.timmitof.feature_home.presentation.R

/**
 * Инструкция по подключению клавиатуры: заголовок, два шага и карточка «готово».
 *
 * Шаги идут строго по порядку — второй разблокируется только после первого,
 * так пользователь не попадёт в пустой системный диалог выбора клавиатуры.
 *
 * @param setup текущее состояние подключения.
 * @param onStepClick переход в системный экран нужного шага.
 */
@Composable
internal fun KeyboardSetupGuide(
    setup: KeyboardSetupModel,
    onStepClick: (KeyboardSetupStep) -> Unit,
    modifier: Modifier = Modifier
) {
    val isReady = setup.currentStep == KeyboardSetupStep.DONE

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SetupHeader(isReady = isReady)

        SetupStepCard(
            number = 1,
            title = stringResource(R.string.setup_step_enable_title),
            description = stringResource(R.string.setup_step_enable_description),
            actionTitle = stringResource(R.string.setup_step_enable_action),
            status = if (setup.isEnabled) SetupStepStatus.DONE else SetupStepStatus.ACTIVE,
            onAction = { onStepClick(KeyboardSetupStep.ENABLE) }
        )

        SetupStepCard(
            number = 2,
            title = stringResource(R.string.setup_step_select_title),
            description = stringResource(R.string.setup_step_select_description),
            actionTitle = stringResource(R.string.setup_step_select_action),
            status = when {
                setup.isSelected -> SetupStepStatus.DONE
                setup.isEnabled -> SetupStepStatus.ACTIVE
                else -> SetupStepStatus.LOCKED
            },
            onAction = { onStepClick(KeyboardSetupStep.SELECT) }
        )

        AnimatedVisibility(
            visible = isReady,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            SetupDoneCard()
        }
    }
}

/** Шапка инструкции: иконка клавиатуры и краткое объяснение, зачем это всё. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SetupHeader(
    isReady: Boolean,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + scaleIn(
                initialScale = 0.8f,
                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()
            )
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_keyboard_off),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Text(
            text = stringResource(
                if (isReady) R.string.setup_title_ready else R.string.setup_title
            ),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(
                if (isReady) R.string.setup_subtitle_ready else R.string.setup_subtitle
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** Карточка успешного подключения — появляется, когда оба шага пройдены. */
@Composable
private fun SetupDoneCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null
            )
            Text(
                text = stringResource(R.string.setup_done_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.setup_done_description),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KeyboardSetupGuidePreview() {
    KeyboardFontsTheme {
        KeyboardSetupGuide(
            setup = KeyboardSetupModel(isEnabled = true),
            onStepClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
