package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.ui.text.PlatformTextStyle

/** Подсказка в углу клавиши прижата к краю: на Android без лишнего отступа шрифта, на iOS его нет. */
internal expect val KeyLabelPlatformStyle: PlatformTextStyle?
