package kg.timmitof.keyboard.engine

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import dagger.hilt.android.AndroidEntryPoint
import kg.timmitof.keyboard.domain.model.TextContext
import kg.timmitof.keyboard.domain.repository.ClipboardRepository
import kg.timmitof.keyboard.domain.repository.EmojiRepository
import kg.timmitof.keyboard.domain.repository.FontRepository
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository
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

    @Inject
    lateinit var keyboardSettingsRepository: KeyboardSettingsRepository

    @Inject
    lateinit var clipboardRepository: ClipboardRepository

    private val actionHandler = KeyboardActionHandler(
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
        scheduleTextSync()
    }

    /**
     * Откладывает чтение поля до паузы в наборе.
     *
     * Во время быстрого набора поле присылает событие на каждый символ, а свой
     * снимок клавиатура и так ведёт сама — читать чужой процесс по десять раз
     * в секунду незачем.
     */
    private fun scheduleTextSync() {
        textSyncHandler.removeCallbacks(textSyncTask)
        textSyncHandler.postDelayed(textSyncTask, TEXT_SYNC_DELAY_MILLIS)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        textSyncHandler.removeCallbacks(textSyncTask)
        keyboardView?.updateTextContext(TextContext())
    }

    /**
     * Применяет действие к полю.
     *
     * После обычного ввода поле не перечитывается: клавиатура сама знает, что
     * напечатала, и обновляет свой снимок текста мгновенно. Чтение через
     * `InputConnection` — это блокирующий вызов в чужой процесс, и на каждом
     * нажатии он превращается в заметную задержку.
     *
     * Перечитываем только после правок, результат которых клавиатуре
     * неизвестен: удаление слова, работа с выделением, движение курсора.
     */
    private fun applyAction(action: KeyboardSideEffect) {
        when (action) {
            is KeyboardSideEffect.Input -> {
                actionHandler.handle(action)
                if (action.needsTextResync) scheduleTextSync()
            }

            KeyboardSideEffect.OpenApp -> openApp()
        }
    }

    /** Правки, после которых снимок текста надо перечитать из поля. */
    private val KeyboardSideEffect.Input.needsTextResync: Boolean
        get() = when (this) {
            is KeyboardSideEffect.Input.CommitText,
            is KeyboardSideEffect.Input.SelectBeforeCursor,
                -> false

            else -> true
        }

    /** Переход в приложение из листа настроек или буфера. */
    private fun openApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ?: return

        runCatching { startActivity(intent) }
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
        textSyncHandler.removeCallbacks(textSyncTask)
        keyboardView = null
    }

    private companion object {
        /** Окно текста до курсора: хватает и на слово, и на лексику сообщения. */
        const val BEFORE_LENGTH = TextContext.MAX_BEFORE_LENGTH

        /** Пауза в наборе, после которой снимок текста сверяется с полем. */
        const val TEXT_SYNC_DELAY_MILLIS = 60L

        /** После курсора важно лишь то, стоит ли он внутри слова. */
        const val AFTER_LENGTH = 32
    }
}
