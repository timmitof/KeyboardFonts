package kg.timmitof.keyboard.integration

import android.app.Activity

interface KeyboardContract {
    fun isKeyboardEnabled(): Boolean
    fun isKeyboardSelected(): Boolean
    fun openKeyboardSettings(activity: Activity)
}