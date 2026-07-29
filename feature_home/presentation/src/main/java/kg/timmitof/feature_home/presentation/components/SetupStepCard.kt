package kg.timmitof.feature_home.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kg.timmitof.core.ui.theme.KeyboardFontsTheme
import kg.timmitof.feature_home.presentation.R

/**
 * Состояние шага инструкции: пройден, активен (можно выполнять) или ждёт предыдущего шага.
 */
internal enum class SetupStepStatus { DONE, ACTIVE, LOCKED }

/**
 * Карточка одного шага подключения клавиатуры.
 *
 * Активный шаг подсвечен и показывает кнопку действия, пройденный — сворачивается до галочки,
 * заблокированный приглушён, чтобы вести пользователя строго по порядку.
 *
 * @param number порядковый номер шага (показывается, пока шаг не пройден).
 * @param title краткое название шага.
 * @param description что именно нужно сделать в системном экране.
 * @param actionTitle подпись кнопки перехода в системные настройки.
 * @param status текущее состояние шага.
 * @param onAction переход в системный экран этого шага.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SetupStepCard(
    number: Int,
    title: String,
    description: String,
    actionTitle: String,
    status: SetupStepStatus,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = status == SetupStepStatus.DONE
    val isActive = status == SetupStepStatus.ACTIVE

    val containerColor by animateColorAsState(
        targetValue = when (status) {
            SetupStepStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
            SetupStepStatus.DONE -> MaterialTheme.colorScheme.surfaceContainerLow
            SetupStepStatus.LOCKED -> MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "stepContainerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = when (status) {
            SetupStepStatus.ACTIVE -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "stepContentColor"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (status == SetupStepStatus.LOCKED) LOCKED_ALPHA else 1f,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "stepContentAlpha"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                // альфа лямбдой на модификаторе — анимация без рекомпозиции содержимого
                .alpha(contentAlpha),
            verticalAlignment = Alignment.Top
        ) {
            StepBadge(
                number = number,
                isDone = isDone
            )

            Spacer(modifier = Modifier.size(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )

                AnimatedVisibility(
                    visible = !isDone,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onAction,
                            enabled = isActive,
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text(text = actionTitle)
                        }
                    }
                }
            }
        }
    }
}

/** Кружок с номером шага, превращающийся в галочку после его прохождения. */
@Composable
private fun StepBadge(
    number: Int,
    isDone: Boolean,
    modifier: Modifier = Modifier
) {
    val badgeColor by animateColorAsState(
        targetValue = if (isDone) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "stepBadgeColor"
    )
    val outlineColor = LocalContentColor.current

    Surface(
        modifier = modifier.size(32.dp),
        shape = CircleShape,
        color = badgeColor,
        contentColor = if (isDone) MaterialTheme.colorScheme.onPrimary else outlineColor,
        border = if (isDone) null else BorderStroke(1.dp, outlineColor.copy(alpha = OUTLINE_ALPHA))
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.setup_step_done),
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private const val LOCKED_ALPHA = 0.45f
private const val OUTLINE_ALPHA = 0.4f

@Preview(showBackground = true)
@Composable
private fun SetupStepCardPreview() {
    KeyboardFontsTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SetupStepCard(
                number = 1,
                title = "Включи Keyboard Fonts",
                description = "Найди в списке и переведи переключатель",
                actionTitle = "Открыть настройки",
                status = SetupStepStatus.DONE,
                onAction = {}
            )
            SetupStepCard(
                number = 2,
                title = "Сделай её основной",
                description = "Выбери Keyboard Fonts текущей клавиатурой",
                actionTitle = "Выбрать клавиатуру",
                status = SetupStepStatus.ACTIVE,
                onAction = {}
            )
        }
    }
}
