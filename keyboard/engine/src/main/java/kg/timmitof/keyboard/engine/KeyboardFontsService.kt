package kg.timmitof.keyboard.engine

import android.view.View
import android.view.inputmethod.EditorInfo
import dagger.hilt.android.AndroidEntryPoint
import kg.timmitof.keyboard.domain.repository.EmojiRepository
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import kg.timmitof.keyboard.presentation.KeyboardFontsView
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardViewModelFactory
import javax.inject.Inject

@AndroidEntryPoint
internal class KeyboardFontsService : ComposeInputMethodService() {

    @Inject
    lateinit var keyboardLayoutRepository: KeyboardLayoutRepository

    @Inject
    lateinit var emojiRepository: EmojiRepository

    @Inject
    lateinit var languageRepository: LanguageRepository

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
            languageRepository = languageRepository
        ),
        onKeyboardAction = actionHandler::handle,
    ).also { keyboardView = it }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        keyboardView?.updateEnterAction(attribute.toEnterAction())
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardView = null
    }
}