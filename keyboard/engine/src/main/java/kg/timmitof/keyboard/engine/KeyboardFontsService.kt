package kg.timmitof.keyboard.engine

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import dagger.hilt.android.AndroidEntryPoint
import kg.timmitof.keyboard.suggestion.domain.model.TextContext
import kg.timmitof.keyboard.clipboard.domain.repository.ClipboardRepository
import kg.timmitof.keyboard.domain.repository.EmojiRepository
import kg.timmitof.keyboard.font.domain.repository.FontRepository
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import kg.timmitof.keyboard.suggestion.domain.repository.SuggestionRepository
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

    @Inject
    lateinit var keyboardSettingsRepository: KeyboardSettingsRepository

    @Inject
    lateinit var clipboardRepository: ClipboardRepository

    private val actionHandler = KeyboardActionHandler(
        context = this,
        inputConnectionProvider = { currentInputConnection },
        editorInfoProvider = { currentInputEditorInfo },
    )

    private var keyboardView: KeyboardFontsView? = null

    private val textSyncHandler = Handler(Looper.getMainLooper())

    private val textSyncTask = Runnable { syncTextContext() }

    override fun onCreateComposeView(): View = KeyboardFontsView(
        context = this,
        viewModelStoreOwner = this,
        viewModelFactory = KeyboardViewModelFactory(
            keyboardLayoutRepository = keyboardLayoutRepository,
            emojiRepository = emojiRepository,
            languageRepository = languageRepository,
            fontRepository = fontRepository,
            suggestionRepository = suggestionRepository,
            keyboardSettingsRepository = keyboardSettingsRepository,
            clipboardRepository = clipboardRepository,
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
        scheduleTextSync()
    }

    /** Читаем поле после паузы в наборе: свой снимок ведём сами, а чтение чужого процесса на каждый символ дорого. */
    private fun scheduleTextSync() {
        textSyncHandler.removeCallbacks(textSyncTask)
        textSyncHandler.postDelayed(textSyncTask, TEXT_SYNC_DELAY_MILLIS)
    }

    /** Система по умолчанию в альбомном режиме раскрывает ввод на весь экран; мы показываем обычную клавиатуру поверх приложения. */
    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        textSyncHandler.removeCallbacks(textSyncTask)
        keyboardView?.updateTextContext(TextContext())
    }

    /**
     * После обычного ввода поле не перечитываем: `InputConnection` — блокирующий вызов
     * в чужой процесс, на каждом нажатии это задержка. Только после правок с неизвестным результатом.
     */
    private fun applyAction(action: KeyboardSideEffect) {
        when (action) {
            is KeyboardSideEffect.Input -> {
                actionHandler.handle(action)
                if (action.needsTextResync) scheduleTextSync()
            }

            KeyboardSideEffect.OpenApp -> openApp()
            KeyboardSideEffect.HideKeyboard -> requestHideSelf(0)
        }
    }

    private val KeyboardSideEffect.Input.needsTextResync: Boolean
        get() = when (this) {
            is KeyboardSideEffect.Input.CommitText,
            is KeyboardSideEffect.Input.SetComposingText,
            is KeyboardSideEffect.Input.FinishComposing,
            is KeyboardSideEffect.Input.SelectBeforeCursor,
                -> false

            else -> true
        }

    private fun openApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ?: return

        runCatching { startActivity(intent) }
    }

    private fun syncFieldContext() {
        keyboardView?.updateFieldContext(currentInputEditorInfo.toFieldContext())
    }

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
        textSyncHandler.removeCallbacks(textSyncTask)
        keyboardView = null
    }

    private companion object {
        const val BEFORE_LENGTH = TextContext.MAX_BEFORE_LENGTH

        const val TEXT_SYNC_DELAY_MILLIS = 60L

        const val AFTER_LENGTH = 32
    }
}
