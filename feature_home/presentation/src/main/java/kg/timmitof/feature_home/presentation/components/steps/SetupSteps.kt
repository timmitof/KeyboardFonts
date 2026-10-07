package kg.timmitof.feature_home.presentation.components.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.R as UiR
import kg.timmitof.core.ui.theme.appColors

@DslMarker
annotation class SetupDsl

enum class SetupStepStatus { DONE, ACTIVE, PENDING }

@Immutable
internal class SetupStep(
    val number: Int,
    val title: String,
    val subtitle: String,
    val status: SetupStepStatus,
    val content: (@Composable ColumnScope.() -> Unit)?,
)

@SetupDsl
class SetupStepsScope internal constructor() {

    private val steps = mutableListOf<SetupStep>()

    internal fun steps(): List<SetupStep> = steps

    fun step(
        title: String,
        subtitle: String,
        status: SetupStepStatus,
        content: (@Composable ColumnScope.() -> Unit)? = null,
    ) {
        steps += SetupStep(
            number = steps.size + 1,
            title = title,
            subtitle = subtitle,
            status = status,
            content = content,
        )
    }
}

@Composable
fun SetupSteps(
    modifier: Modifier = Modifier,
    content: SetupStepsScope.() -> Unit,
) {
    val steps = SetupStepsScope().apply(content).steps()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        steps.forEach { step ->
            key(step.number) { SetupStepCard(step = step) }
        }
    }
}

@Composable
private fun SetupStepCard(step: SetupStep) {
    val colors = MaterialTheme.appColors
    val isActive = step.status == SetupStepStatus.ACTIVE

    val container by animateColorAsState(
        targetValue = when (step.status) {
            SetupStepStatus.DONE -> colors.success.container
            SetupStepStatus.ACTIVE -> colors.card
            SetupStepStatus.PENDING -> colors.cardMuted
        },
        animationSpec = spring(stiffness = StepStiffness),
        label = "stepContainer",
    )
    // Обводка текущего шага проявляется плавно и рисуется в draw-фазе — без рекомпозиций.
    val outlineAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = spring(stiffness = StepStiffness),
        label = "stepOutline",
    )
    val outline = colors.brand.solid

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(StepShape)
            .drawBehind {
                drawRect(container)
                if (outlineAlpha > 0f) {
                    val width = OutlineWidth.toPx()
                    drawRoundRect(
                        color = outline.copy(alpha = outlineAlpha),
                        topLeft = Offset(width / 2, width / 2),
                        size = Size(size.width - width, size.height - width),
                        cornerRadius = CornerRadius(StepRadius.toPx() - width / 2),
                        style = Stroke(width),
                    )
                }
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        StepHeader(step = step)

        AnimatedVisibility(
            visible = isActive && step.content != null,
            enter = fadeIn(spring(stiffness = StepStiffness)) +
                expandVertically(spring(dampingRatio = 0.9f, stiffness = StepStiffness)),
            exit = fadeOut(spring(stiffness = StepStiffness)) +
                shrinkVertically(spring(dampingRatio = 0.9f, stiffness = StepStiffness)),
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                step.content?.invoke(this)
            }
        }
    }
}

@Composable
private fun StepHeader(step: SetupStep) {
    val isDone = step.status == SetupStepStatus.DONE

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepBadge(number = step.number, status = step.status)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.title,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                modifier = Modifier.padding(top = 1.dp),
                text = step.subtitle,
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                color = if (isDone) MaterialTheme.appColors.success.solid else MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun StepBadge(number: Int, status: SetupStepStatus) {
    val colors = MaterialTheme.appColors

    val background by animateColorAsState(
        targetValue = when (status) {
            SetupStepStatus.DONE -> colors.success.solid
            SetupStepStatus.ACTIVE -> colors.brand.solid
            SetupStepStatus.PENDING -> MaterialTheme.colorScheme.surfaceContainerHighest
        },
        animationSpec = spring(stiffness = StepStiffness),
        label = "stepBadge",
    )
    val content = when (status) {
        SetupStepStatus.DONE -> colors.success.onSolid
        SetupStepStatus.ACTIVE -> colors.brand.onSolid
        SetupStepStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .drawBehind { drawRect(background) },
        contentAlignment = Alignment.Center,
    ) {
        if (status == SetupStepStatus.DONE) {
            Icon(
                painter = painterResource(UiR.drawable.ic_check),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(15.dp),
            )
        } else {
            Text(
                text = number.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = content,
            )
        }
    }
}

private val StepRadius = 20.dp
private val StepShape = RoundedCornerShape(StepRadius)
private val OutlineWidth = 2.dp

private const val StepStiffness = 500f
