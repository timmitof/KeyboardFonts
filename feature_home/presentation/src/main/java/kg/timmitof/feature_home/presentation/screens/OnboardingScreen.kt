package kg.timmitof.feature_home.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import kg.timmitof.core.ui.R as UiR
import kg.timmitof.core.ui.base.Container
import kg.timmitof.core.ui.base.ContainerDSLBuilder
import kg.timmitof.core.ui.components.brand.BrandHeader
import kg.timmitof.core.ui.components.brand.StatusPill
import kg.timmitof.core.ui.components.hint.HintCard
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.core.ui.theme.KeyboardFontsTheme
import kg.timmitof.core.ui.theme.appColors
import kg.timmitof.feature_home.domain.model.KeyboardSetupModel
import kg.timmitof.feature_home.domain.model.KeyboardSetupStep
import kg.timmitof.feature_home.presentation.R
import kg.timmitof.feature_home.presentation.components.CoachMark
import kg.timmitof.feature_home.presentation.components.TryFontsField
import kg.timmitof.feature_home.presentation.components.steps.SetupStepStatus
import kg.timmitof.feature_home.presentation.components.steps.SetupSteps
import kg.timmitof.feature_home.presentation.components.steps.SystemMock

/**
 * Первый запуск: весь путь подключения на одном экране, затем проба шрифтов.
 *
 * Шаги засчитываются сами при возврате из системных настроек. Когда клавиатура
 * подключена, экран превращается в пробу: поле в фокусе, живая клавиатура открыта.
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    Container(
        modifier = Modifier.fillMaxSize(),
        viewModel = viewModel,
    ) { state, innerPadding ->
        OnboardingContent(
            state = state,
            innerPadding = innerPadding
        )
    }
}

@Composable
internal fun ContainerDSLBuilder<OnboardingSideEffect, OnboardingEvent>.OnboardingContent(
    state: State<OnboardingState>,
    innerPadding: PaddingValues = PaddingValues()
) {
    // Статус клавиатуры меняется в системных настройках — перечитываем его при каждом возврате
    LifecycleResumeEffect(Unit) {
        sendEvent(OnboardingEvent.ScreenResumed)
        onPauseOrDispose { }
    }

    val onStepAction = remember<(KeyboardSetupStep) -> Unit> {
        { step -> sendEvent(OnboardingEvent.StepActionClicked(step)) }
    }
    val onDone = remember { { sendEvent(OnboardingEvent.DoneClicked) } }

    val setup = state.value.setup
    val isReady = setup.currentStep == KeyboardSetupStep.DONE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
            .padding(horizontal = HorizontalPadding)
    ) {
        BrandHeader(title = stringResource(R.string.app_title)) {
            AnimatedVisibility(
                visible = isReady,
                enter = fadeIn() + scaleIn(spring(dampingRatio = 0.6f, stiffness = 500f)),
                exit = fadeOut(),
            ) {
                StatusPill(
                    text = stringResource(R.string.onboarding_status_connected),
                    role = AccentRole.SUCCESS,
                )
            }
        }

        AnimatedContent(
            modifier = Modifier.weight(1f),
            targetState = isReady,
            transitionSpec = {
                (fadeIn(spring(stiffness = 400f)) + slideInVertically(spring(stiffness = 400f)) { it / 10 })
                    .togetherWith(fadeOut(spring(stiffness = 900f)))
            },
            label = "onboardingPhase",
        ) { isTrying ->
            if (isTrying) {
                TryContent(onDone = onDone)
            } else {
                SetupContent(setup = setup, onStepAction = onStepAction)
            }
        }
    }
}

/** Шаги подключения и янтарная подсказка к текущему шагу. */
@Composable
private fun SetupContent(
    setup: KeyboardSetupModel,
    onStepAction: (KeyboardSetupStep) -> Unit,
) {
    val isEnableStep = setup.currentStep == KeyboardSetupStep.ENABLE

    val enableStatus = if (setup.isEnabled) SetupStepStatus.DONE else SetupStepStatus.ACTIVE
    val selectStatus = when {
        setup.isSelected -> SetupStepStatus.DONE
        setup.isEnabled -> SetupStepStatus.ACTIVE
        else -> SetupStepStatus.PENDING
    }

    val enableTitle = stringResource(
        if (setup.isEnabled) R.string.step_enable_done_title else R.string.step_enable_title
    )
    val enableSubtitle = stringResource(
        if (setup.isEnabled) R.string.step_enable_done_subtitle else R.string.step_enable_subtitle
    )
    val enableCaption = stringResource(R.string.step_enable_mock_caption)
    val enablePoke = stringResource(R.string.step_enable_mock_poke)
    val enableAction = stringResource(R.string.step_enable_action)

    val selectTitle = stringResource(R.string.step_select_title)
    val selectSubtitle = stringResource(
        if (selectStatus == SetupStepStatus.ACTIVE) {
            R.string.step_select_subtitle_active
        } else {
            R.string.step_select_subtitle
        }
    )
    val selectCaption = stringResource(R.string.step_select_mock_caption)
    val selectPoke = stringResource(R.string.step_select_mock_poke)
    val selectAction = stringResource(R.string.step_select_action)

    val tryTitle = stringResource(R.string.step_try_title)
    val trySubtitle = stringResource(R.string.step_try_subtitle)

    val otherKeyboard = stringResource(R.string.mock_other_keyboard)
    val ourKeyboard = stringResource(R.string.mock_our_keyboard)

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedContent(
                targetState = isEnableStep,
                transitionSpec = { fadeIn(spring(stiffness = 500f)).togetherWith(fadeOut(spring(stiffness = 900f))) },
                label = "setupTitle",
            ) { isEnable ->
                ScreenTitle(
                    title = stringResource(
                        if (isEnable) R.string.onboarding_enable_title else R.string.onboarding_select_title
                    ),
                    lead = stringResource(
                        if (isEnable) R.string.onboarding_enable_lead else R.string.onboarding_select_lead
                    ),
                )
            }

            SetupSteps(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)) {
                step(title = enableTitle, subtitle = enableSubtitle, status = enableStatus) {
                    SystemMock(caption = enableCaption) {
                        toggle(label = otherKeyboard, checked = true)
                        toggle(label = ourKeyboard, checked = false, poke = enablePoke)
                    }
                    PrimaryButton(text = enableAction) { onStepAction(KeyboardSetupStep.ENABLE) }
                }
                step(title = selectTitle, subtitle = selectSubtitle, status = selectStatus) {
                    SystemMock(caption = selectCaption) {
                        radio(label = otherKeyboard, selected = false)
                        radio(label = ourKeyboard, selected = true, poke = selectPoke)
                    }
                    PrimaryButton(text = selectAction) { onStepAction(KeyboardSetupStep.SELECT) }
                }
                step(title = tryTitle, subtitle = trySubtitle, status = SetupStepStatus.PENDING)
            }
        }

        // Янтарные карточки отвечают на главные причины бросить настройку.
        AnimatedContent(
            modifier = Modifier.padding(bottom = 16.dp),
            targetState = isEnableStep,
            transitionSpec = { fadeIn(spring(stiffness = 500f)).togetherWith(fadeOut(spring(stiffness = 900f))) },
            label = "setupHint",
        ) { isEnable ->
            if (isEnable) {
                HintCard(
                    title = stringResource(R.string.hint_warning_title),
                    body = stringResource(R.string.hint_warning_body),
                    icon = painterResource(UiR.drawable.ic_shield_check),
                )
            } else {
                HintCard(
                    title = stringResource(R.string.hint_switch_back_title),
                    body = stringResource(R.string.hint_switch_back_body),
                    icon = painterResource(UiR.drawable.ic_lightbulb),
                )
            }
        }
    }
}

/** Проба: поле в фокусе, клавиатура открыта, подсказка показывает на строку шрифтов. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TryContent(onDone: () -> Unit) {
    val success = MaterialTheme.appColors.success

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenTitle(
            title = stringResource(R.string.onboarding_try_title),
            lead = stringResource(R.string.onboarding_try_lead),
        )

        TryFontsField(
            modifier = Modifier.padding(top = 16.dp),
            placeholder = stringResource(R.string.try_placeholder),
        )

        Button(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .height(46.dp),
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(
                containerColor = success.container,
                contentColor = success.onContainer,
            ),
        ) {
            Text(text = stringResource(R.string.try_done), fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
            Icon(
                painter = painterResource(UiR.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(16.dp),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Подсказка нужна, только пока клавиатура на экране — она указывает на её строку шрифтов.
        AnimatedVisibility(
            visible = WindowInsets.isImeVisible,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut(),
        ) {
            CoachMark(
                modifier = Modifier.padding(bottom = 6.dp),
                title = stringResource(R.string.coach_title),
                body = stringResource(R.string.coach_body),
            )
        }
    }
}

@Composable
private fun ScreenTitle(title: String, lead: String) {
    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontSize = 25.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = lead,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        onClick = onClick,
    ) {
        Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

private val HorizontalPadding = 16.dp

@Preview(showBackground = true)
@Composable
private fun OnboardingPreview() {
    KeyboardFontsTheme {
        Surface {
            ContainerDSLBuilder<OnboardingSideEffect, OnboardingEvent>({}).OnboardingContent(
                state = remember { mutableStateOf(OnboardingState(KeyboardSetupModel(isEnabled = true))) }
            )
        }
    }
}
