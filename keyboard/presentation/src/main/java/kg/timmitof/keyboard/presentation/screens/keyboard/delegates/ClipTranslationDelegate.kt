package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.clipboard.domain.repository.ClipboardRepository
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kg.timmitof.keyboard.presentation.screens.keyboard.states.ClipTranslation
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import kg.timmitof.keyboard.presentation.screens.keyboard.states.LanguageDetectionRules
import kg.timmitof.keyboard.translate.domain.model.TranslationResult
import kg.timmitof.keyboard.translate.domain.repository.LanguageDetectionRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslationModelRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslationRepository

/**
 * Перевод скопированного (дизайн 8b, «Скопировали чужое сообщение»): клавиатура открылась, а в буфере —
 * только что скопированный текст на другом языке. Предлагаем перевод на язык раскладки карточкой над клавишами.
 *
 * Работы — один раз на копию: время копирования смотрим без чтения буфера, язык определяем и переводим
 * только свежую копию и только если обе модели уже на телефоне (молча ничего не качаем).
 */
internal class ClipTranslationDelegate(
    private val clipboardRepository: ClipboardRepository,
    private val detectionRepository: LanguageDetectionRepository,
    private val translationRepository: TranslationRepository,
    private val modelRepository: TranslationModelRepository,
) {

    /** Копия, которую уже разбирали: одну и ту же не переводим на каждый показ клавиатуры. */
    @Volatile
    private var handledCopiedAt = 0L

    suspend fun KeyboardSyntax.offer() {
        if (!state.allowsTranslation || state.translator != null) return

        val clip = clipboardRepository.getFreshClip() ?: return
        if (clip.copiedAt == handledCopiedAt) return
        handledCopiedAt = clip.copiedAt

        try {
            val target = state.activeLanguage?.let { modelRepository.languageForLayout(it.code) } ?: return
            val detected = detectionRepository.detect(clip.text.take(LanguageDetectionRules.MAX_TEXT_LENGTH))
            // Скопировано на языке раскладки — переводить нечего.
            val source = LanguageDetectionRules.confidentCode(detected)
                ?.takeIf { it != target.code }
                ?.let(modelRepository::language)
                ?: return

            val downloaded = modelRepository.downloadedCodes()
            if (source.code !in downloaded || target.code !in downloaded) return

            val result = translationRepository.translate(clip.text, source.code, target.code)
            if (result !is TranslationResult.Success || state.translator != null) return

            reduce { state.copy(clipTranslation = ClipTranslation(clip.text, result.text, source)) }
        } finally {
            // Панель перевода закрыта — моделям в памяти делать нечего.
            if (state.translator == null) {
                detectionRepository.release()
                translationRepository.release()
            }
        }
    }

    /** «Вставить перевод»: как вставка из буфера, только переводом. */
    suspend fun KeyboardSyntax.paste() {
        val clip = state.clipTranslation ?: return

        input(KeyboardSideEffect.Input.CommitText(clip.translation))
        dismiss()
    }

    suspend fun KeyboardSyntax.dismiss() {
        if (state.clipTranslation != null) reduce { state.copy(clipTranslation = null) }
    }
}
