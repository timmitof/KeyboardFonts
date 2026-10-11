package kg.timmitof.keyboardfonts.analytics

import android.util.Log
import kg.timmitof.core.analytics.Analytics
import kg.timmitof.core.analytics.AnalyticsEvent

/** Debug-сборка: события видны в Logcat по тегу `Analytics`, в Firebase ничего не уходит. */
internal object LogcatAnalytics : Analytics {

    private const val TAG = "Analytics"

    override fun log(event: AnalyticsEvent) {
        Log.d(TAG, "${event.name} ${event.params}")
    }

    override fun setUserProperty(name: String, value: String?) {
        Log.d(TAG, "user property $name = $value")
    }

    override fun recordError(throwable: Throwable) {
        Log.w(TAG, "non-fatal", throwable)
    }
}
