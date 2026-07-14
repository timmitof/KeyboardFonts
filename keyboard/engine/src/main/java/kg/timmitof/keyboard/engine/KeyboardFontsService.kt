package kg.timmitof.keyboard.engine

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dagger.hilt.android.AndroidEntryPoint
import kg.timmitof.keyboard.domain.repository.EmojiRepository
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kg.timmitof.keyboard.presentation.KeyboardFontsView
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSideEffect
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardViewModelFactory
import javax.inject.Inject

@AndroidEntryPoint
internal class KeyboardFontsService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    @Inject
    lateinit var keyboardLayoutRepository: KeyboardLayoutRepository

    @Inject
    lateinit var emojiRepository: EmojiRepository

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore = ViewModelStore()

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onCreateInputView(): View {
        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
        }

        val factory = KeyboardViewModelFactory(
            keyboardLayoutRepository = keyboardLayoutRepository,
            emojiRepository = emojiRepository
        )

        return KeyboardFontsView(
            context = this,
            viewModelStoreOwner = this,
            viewModelFactory = factory,
            onKeyboardAction = ::handleKeyboardAction,
        )
    }

    private fun handleKeyboardAction(action: KeyboardSideEffect) {
        val inputConnection = currentInputConnection ?: return
        when (action) {
            is KeyboardSideEffect.CommitText -> {
                inputConnection.commitText(action.char, 1)
            }
            is KeyboardSideEffect.DeleteBackward -> {
                inputConnection.deleteSurroundingText(1, 0)
            }
            is KeyboardSideEffect.PerformEditorAction -> {
                val editorAction = currentInputEditorInfo?.imeOptions
                    ?.and(EditorInfo.IME_MASK_ACTION)
                    ?: EditorInfo.IME_ACTION_UNSPECIFIED
                inputConnection.performEditorAction(editorAction)
            }
        }
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
        handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelStore.clear()
        handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    private fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }
}