package kg.timmitof.keyboard.engine

import android.view.View
import android.view.inputmethod.EditorInfo
import dagger.hilt.android.AndroidEntryPoint
import kg.timmitof.keyboard.domain.model.TextContext
import kg.timmitof.keyboard.domain.repository.EmojiRepository
import kg.timmitof.keyboard.domain.repository.FontRepository
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import kg.timmitof.keyboard.domain.repository.SuggestionRepository
import kg.timmitof.keyboard.presentation.KeyboardFontsView
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardViewModelFactory
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import javax.inject.Inject

@AndroidEntryPoint
internal class KeyboardFontsService : ComposeInputMethodService() {

    @Inject
    lateinit var keyboardLayoutRepository: KeyboardLayoutRepository

    @Inject
    lateinit var emojiRepository: EmojiRepository

    @Inject
    lateinit var languageRepository: LanguageRepository

    @Inject
    lateinit var fontRepository: FontRepository

    @Inject
    lateinit var suggestionRepository: SuggestionRepository

    private val actionHandler = KeyboardActionHandler(
        inputConnectionProvider = { currentInputConnection },
        editorInfoProvider = { currentInputEditorInfo },
    )

    private var keyboardView: KeyboardFontsView? = null

    override fun onCreateComposeView(): View = KeyboardFontsView(
        context = this,
        viewModelStoreOwner = this,
        viewModelFactory = KeyboardViewModelFactory(
            keyboardLayoutRepository = keyboardLayoutRepository,
            emojiRepository = emojiRepository,
            languageRepository = languageRepository,
            fontRepository = fontRepository,
            suggestionRepository = suggestionRepository,
        ),
        onKeyboardAction = ::applyAction,
    ).also { keyboardView = it }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        syncFieldContext()
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
        syncFieldContext()
        syncTextContext()
    }

    /**
     * Поле сообщило о новой позиции курсора — значит, изменился и контекст подсказок.
     */
    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int,
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd,
        )
        syncTextContext()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        keyboardView?.updateTextContext(TextContext())
    }

    /**
     * Применяет действие к полю и сразу перечитывает контекст.
     *
     * Некоторые поля не присылают `onUpdateSelection` на каждую правку,
     * а подсказки должны обновляться после любого нажатия.
     */
    private fun applyAction(action: KeyboardSideEffect) {
        actionHandler.handle(action)
        syncTextContext()
    }

    private fun syncFieldContext() {
        keyboardView?.updateFieldContext(currentInputEditorInfo.toFieldContext())
    }

    /** Снимает окно текста вокруг курсора — вход Т9. */
    private fun syncTextContext() {
        val view = keyboardView ?: return
        val connection = currentInputConnection

        if (connection == null) {
            view.updateTextContext(TextContext())
            return
        }

        view.updateTextContext(
            TextContext(
                before = connection.getTextBeforeCursor(BEFORE_LENGTH, 0)?.toString().orEmpty(),
                after = connection.getTextAfterCursor(AFTER_LENGTH, 0)?.toString().orEmpty(),
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardView = null
    }

    private companion object {
        /** Окно текста до курсора: хватает и на слово, и на лексику сообщения. */
        const val BEFORE_LENGTH = 512

        /** После курсора важно лишь то, стоит ли он внутри слова. */
        const val AFTER_LENGTH = 32
    }
}
