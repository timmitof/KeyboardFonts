package kg.timmitof.feature_settings.presentation.screens.tipjar

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kg.timmitof.core.ui.base.ContainerDSLBuilder
import kg.timmitof.core.ui.base.SheetContainer
import kg.timmitof.core.ui.components.brand.AccentIconTile
import kg.timmitof.core.ui.components.settings.SettingsPillButton
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.core.ui.theme.appColors
import kg.timmitof.feature_settings.domain.model.Tip
import kg.timmitof.feature_settings.domain.model.TipOffer
import kg.timmitof.feature_settings.presentation.R
import kg.timmitof.core.ui.R as CoreR

@Composable
fun TipJarScreen(
    onDismiss: () -> Unit,
    viewModel: TipJarViewModel = hiltViewModel(),
) {
    SheetContainer(
        viewModel = viewModel,
        onDismiss = onDismiss,
    ) { state ->
        TipJarContent(state = state)
    }
}

@Composable
private fun ContainerDSLBuilder<TipJarSideEffect, TipJarEvent>.TipJarContent(state: State<TipJarState>) {
    val haptics = LocalHapticFeedback.current
    handleSideEffect { effect ->
        when (effect) {
            TipJarSideEffect.Thanked -> haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        }
    }
    LaunchedEffect(Unit) { sendEvent(TipJarEvent.Shown) }

    val thanks by remember(state) { derivedStateOf { state.value.phase as? TipJarPhase.Thanks } }

    AnimatedContent(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        targetState = thanks?.tip,
        transitionSpec = {
            (fadeIn(spring(stiffness = 500f)) + scaleIn(spring(dampingRatio = 0.8f, stiffness = 500f), initialScale = 0.94f))
                .togetherWith(fadeOut(tween(90)))
                .using(SizeTransform(clip = false))
        },
        label = "tipJarPhase",
    ) { thankedTip ->
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (thankedTip == null) {
                PickingContent(state = state)
            } else {
                ThanksContent(tip = thankedTip)
            }
        }
    }
}

@Composable
private fun ContainerDSLBuilder<TipJarSideEffect, TipJarEvent>.PickingContent(state: State<TipJarState>) {
    val activity = LocalActivity.current
    val current = state.value

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AccentIconTile(
            icon = painterResource(R.drawable.ic_tip_jar),
            role = AccentRole.HINT,
            size = 48.dp,
            iconSize = 26.dp,
            cornerRadius = 16.dp,
        )
        CloseButton(onClick = { sendEvent(TipJarEvent.CloseClicked) })
    }

    Text(
        text = stringResource(R.string.tipjar_title),
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
        modifier = Modifier.padding(bottom = 2.dp),
        text = stringResource(R.string.tipjar_body),
        fontSize = 13.5.sp,
        lineHeight = 20.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    when (val phase = current.phase) {
        TipJarPhase.Loading -> TipsSkeleton()
        is TipJarPhase.Failed -> FailedBlock(
            message = stringResource(phase.failure.messageRes),
            onRetry = if (phase.failure.canRetry) ({ sendEvent(TipJarEvent.RetryClicked) }) else null,
        )
        TipJarPhase.Picking, is TipJarPhase.Thanks -> TipsList(
            tips = current.tips,
            selected = current.selected,
            enabled = !current.isPaying,
            onSelect = { sendEvent(TipJarEvent.TipSelected(it)) },
        )
    }

    val offer = current.selectedOffer
    PrimaryButton(
        text = if (offer != null) stringResource(R.string.tipjar_pay, offer.formattedPrice) else stringResource(R.string.tipjar_pay_loading),
        enabled = current.phase == TipJarPhase.Picking && offer != null && !current.isPaying && activity != null,
        isLoading = current.isPaying,
        onClick = { activity?.let { sendEvent(TipJarEvent.PayClicked(it)) } },
    )

    when {
        current.paymentFailure != null -> StatusLine(
            text = stringResource(current.paymentFailure.messageRes),
            color = MaterialTheme.colorScheme.error,
        )
        current.isPending -> StatusLine(
            text = stringResource(R.string.tipjar_pending),
            color = MaterialTheme.appColors.hint.solid,
        )
    }

    Text(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.tipjar_note),
        fontSize = 11.5.sp,
        lineHeight = 16.sp,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
private fun TipsList(
    tips: List<TipOffer>,
    selected: Tip,
    enabled: Boolean,
    onSelect: (Tip) -> Unit,
) {
    Column(
        modifier = Modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tips.forEach { offer ->
            key(offer.tip) {
                TipRow(
                    offer = offer,
                    isSelected = offer.tip == selected,
                    enabled = enabled,
                    onClick = { onSelect(offer.tip) },
                )
            }
        }
    }
}

@Composable
private fun TipRow(
    offer: TipOffer,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.appColors
    val style = offer.tip.style
    val selectedBackground = lerp(colors.card, colors.hint.container, 0.55f)
    val background by animateColorAsState(
        targetValue = if (isSelected) selectedBackground else colors.card,
        animationSpec = spring(stiffness = 600f),
        label = "tipBackground",
    )
    val border by animateColorAsState(
        targetValue = if (isSelected) colors.hint.solid else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = spring(stiffness = 600f),
        label = "tipBorder",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(TipShape)
            .background(background)
            .border(if (isSelected) 2.dp else 1.dp, border, TipShape)
            .selectable(selected = isSelected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(start = 10.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AccentIconTile(
            icon = painterResource(style.iconRes),
            role = style.role,
            cornerRadius = 13.dp,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(style.titleRes),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = offer.formattedPrice,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

/** Прозрачность — в фазе отрисовки, без рекомпозиций. */
@Composable
private fun TipsSkeleton() {
    val transition = rememberInfiniteTransition(label = "tipSkeleton")
    val pulse by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "tipSkeletonAlpha",
    )
    val color = MaterialTheme.appColors.cardMuted

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(Tip.entries.size) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .graphicsLayer { alpha = pulse }
                    .clip(TipShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun FailedBlock(message: String, onRetry: (() -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(TipShape)
            .background(MaterialTheme.appColors.cardMuted)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        onRetry?.let {
            SettingsPillButton(
                label = stringResource(R.string.tipjar_retry),
                onClick = it,
                role = AccentRole.BRAND,
                isCompact = true,
            )
        }
    }
}

@Composable
private fun ContainerDSLBuilder<TipJarSideEffect, TipJarEvent>.ThanksContent(tip: Tip) {
    val style = tip.style

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        AccentIconTile(
            icon = painterResource(style.iconRes),
            role = AccentRole.APPEARANCE,
            size = 92.dp,
            iconSize = 48.dp,
            cornerRadius = 30.dp,
        )
    }
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(style.thanksRes),
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.tipjar_thanks_body),
        fontSize = 13.5.sp,
        lineHeight = 20.sp,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    SupporterBadgeCard(iconRes = style.iconRes)
    PrimaryButton(
        text = stringResource(R.string.tipjar_done),
        onClick = { sendEvent(TipJarEvent.CloseClicked) },
    )
}

@Composable
private fun SupporterBadgeCard(iconRes: Int) {
    val colors = MaterialTheme.appColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(BadgeShape)
            .background(colors.card)
            .padding(start = 10.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AccentIconTile(
            icon = painterResource(iconRes),
            role = AccentRole.HINT,
            size = 40.dp,
            iconSize = 20.dp,
            cornerRadius = 13.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.tipjar_badge_title),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.tipjar_badge_body),
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(colors.success.solid),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(CoreR.drawable.ic_check),
                contentDescription = null,
                tint = colors.success.onSolid,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled,
        onClick = onClick,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.tipjar_close), onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_close),
            contentDescription = stringResource(R.string.tipjar_close),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun StatusLine(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        fontSize = 12.5.sp,
        lineHeight = 17.sp,
        textAlign = TextAlign.Center,
        color = color,
    )
}

private val TipShape = RoundedCornerShape(18.dp)
private val BadgeShape = RoundedCornerShape(18.dp)
