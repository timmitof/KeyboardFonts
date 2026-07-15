package kg.timmitof.keyboard.engine

import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import kg.timmitof.keyboard.domain.repository.EmojiRepository
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kg.timmitof.keyboard.presentation.KeyboardFontsView
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardViewModelFactory
import javax.inject.Inject

@AndroidEntryPoint
internal class KeyboardFontsService : ComposeInputMethodService() {

    @Inject
    lateinit var keyboardLayoutRepository: KeyboardLayoutRepository

    @Inject
    lateinit var emojiRepository: EmojiRepository

    private val actionHandler = KeyboardActionHandler(
        inputConnectionProvider = { currentInputConnection },
        editorInfoProvider = { currentInputEditorInfo },
    )

    override fun onCreateComposeView(): View = KeyboardFontsView(
        context = this,
        viewModelStoreOwner = this,
        viewModelFactory = KeyboardViewModelFactory(
            keyboardLayoutRepository = keyboardLayoutRepository,
            emojiRepository = emojiRepository
        ),
        onKeyboardAction = actionHandler::handle,
    )
}