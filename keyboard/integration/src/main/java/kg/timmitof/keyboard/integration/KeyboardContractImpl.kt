package kg.timmitof.keyboard.integration

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class KeyboardContractImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : KeyboardContract {

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

    override fun openKeyboardSettings(activity: Activity) {
        runCatching {
            activity.startActivity(INPUT_METHOD_INTENT)
        }.onFailure { e ->
            Log.e("KeyboardContract", "Failed to open keyboard settings", e)
        }
    }

    companion object {
        private val INPUT_METHOD_INTENT = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}