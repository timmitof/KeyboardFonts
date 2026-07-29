package kg.timmitof.keyboard.integration

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class KeyboardContractImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : KeyboardContract {

    private val inputMethodManager: InputMethodManager?
        get() = context.getSystemService(InputMethodManager::class.java)

    override fun isKeyboardEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_INPUT_METHODS
        )
        return enabled?.contains(context.packageName) == true
    }

    override fun isKeyboardSelected(): Boolean {
        val current = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD
        )
        return current?.contains(context.packageName) == true
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

        private val INPUT_METHOD_INTENT = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
