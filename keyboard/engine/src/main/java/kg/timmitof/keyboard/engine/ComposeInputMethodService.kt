package kg.timmitof.keyboard.engine

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
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

/**
 * Базовый [InputMethodService] с поддержкой Compose.
 */
internal abstract class ComposeInputMethodService : InputMethodService(),
    LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner, OnBackPressedDispatcherOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore = ViewModelStore()

    override val onBackPressedDispatcher = OnBackPressedDispatcher()

    protected abstract fun onCreateComposeView(): View

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    final override fun onCreateInputView(): View {
        window?.window?.let { window ->
            window.decorView.apply {
                setViewTreeLifecycleOwner(this@ComposeInputMethodService)
                setViewTreeSavedStateRegistryOwner(this@ComposeInputMethodService)
                setViewTreeViewModelStoreOwner(this@ComposeInputMethodService)
                setViewTreeOnBackPressedDispatcherOwner(this@ComposeInputMethodService)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                onBackPressedDispatcher.setOnBackInvokedDispatcher(window.onBackInvokedDispatcher)
            }
        }
        return onCreateComposeView()
    }

    /**
     * «Назад» до Android 13, где системного диспетчера ещё нет.
     *
     * Событие перехватывается, только когда его кто-то ждёт: иначе клавиатура
     * должна свернуться, как и всегда. Само действие — на отпускании, чтобы
     * долгое нажатие и отмена жеста работали как в системе.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean = when {
        keyCode == KeyEvent.KEYCODE_BACK && onBackPressedDispatcher.hasEnabledCallbacks() -> {
            event.startTracking()
            true
        }

        else -> super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean = when {
        keyCode == KeyEvent.KEYCODE_BACK && onBackPressedDispatcher.hasEnabledCallbacks() -> {
            if (event.isTracking && !event.isCanceled) onBackPressedDispatcher.onBackPressed()
            true
        }

        else -> super.onKeyUp(keyCode, event)
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelStore.clear()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}
