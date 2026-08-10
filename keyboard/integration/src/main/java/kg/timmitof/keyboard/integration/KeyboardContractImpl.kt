package kg.timmitof.keyboard.integration

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class KeyboardContractImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : KeyboardContract {

    private val inputMethodManager: InputMethodManager?
        get() = context.getSystemService(InputMethodManager::class.java)

    override fun getKeyboardState() = KeyboardState(
        isEnabled = isKeyboardEnabled(),
        isSelected = isKeyboardSelected()
    )

    override fun observeKeyboardState(): Flow<KeyboardState> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(getKeyboardState())
            }
        }

        OBSERVED_SETTINGS.forEach { setting ->
            context.contentResolver.registerContentObserver(
                Settings.Secure.getUriFor(setting), false, observer
            )
        }

        trySend(getKeyboardState())

        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }.distinctUntilChanged()

    private fun isKeyboardEnabled(): Boolean =
        inputMethodManager?.enabledInputMethodList.orEmpty()
            .any { it.packageName == context.packageName }

    private fun isKeyboardSelected(): Boolean {
        val current = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD
        ).orEmpty()

        return ComponentName.unflattenFromString(current)?.packageName == context.packageName
    }

    override fun openKeyboardSettings() = runSafely("open keyboard settings") {
        context.startActivity(INPUT_METHOD_INTENT)
    }

    override fun showKeyboardPicker() = runSafely("show keyboard picker") {
        inputMethodManager?.showInputMethodPicker()
    }

    /** Системные экраны могут отсутствовать на кастомных прошивках — падать из-за этого нельзя. */
    private inline fun runSafely(action: String, block: () -> Unit) {
        runCatching(block).onFailure { e -> Log.e(TAG, "Failed to $action", e) }
    }

    companion object {
        private const val TAG = "KeyboardContract"

        private val OBSERVED_SETTINGS = listOf(
            Settings.Secure.ENABLED_INPUT_METHODS,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )

        private val INPUT_METHOD_INTENT = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
