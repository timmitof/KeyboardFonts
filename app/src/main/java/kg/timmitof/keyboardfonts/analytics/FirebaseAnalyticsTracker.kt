package kg.timmitof.keyboardfonts.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kg.timmitof.core.analytics.Analytics
import kg.timmitof.core.analytics.AnalyticsEvent

/** Firebase не принимает Boolean и обрезает длинные строки — приводим значения здесь, а не в каждом событии. */
internal class FirebaseAnalyticsTracker(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics,
) : Analytics {

    override fun log(event: AnalyticsEvent) {
        firebaseAnalytics.logEvent(event.name, event.params.toBundle())
    }

    override fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value?.take(MAX_USER_PROPERTY_LENGTH))
    }

    override fun recordError(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    private fun Map<String, Any>.toBundle(): Bundle = Bundle(size).apply {
        forEach { (key, value) ->
            when (value) {
                is Int -> putLong(key, value.toLong())
                is Long -> putLong(key, value)
                is Float -> putDouble(key, value.toDouble())
                is Double -> putDouble(key, value)
                else -> putString(key, value.toString().take(MAX_PARAM_LENGTH))
            }
        }
    }

    private companion object {
        const val MAX_PARAM_LENGTH = 100
        const val MAX_USER_PROPERTY_LENGTH = 36
    }
}
