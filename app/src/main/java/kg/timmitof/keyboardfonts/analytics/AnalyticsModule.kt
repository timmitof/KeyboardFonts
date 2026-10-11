package kg.timmitof.keyboardfonts.analytics

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kg.timmitof.core.analytics.Analytics
import kg.timmitof.core.analytics.NoOpAnalytics
import kg.timmitof.keyboardfonts.BuildConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    /** Без google-services.json Firebase не инициализируется — тогда сборка работает, но ничего не отправляет. */
    @Singleton
    @Provides
    fun provideAnalytics(@ApplicationContext context: Context): Analytics = when {
        !BuildConfig.ANALYTICS_ENABLED -> LogcatAnalytics
        FirebaseApp.getApps(context).isEmpty() -> NoOpAnalytics
        else -> FirebaseAnalyticsTracker(
            firebaseAnalytics = FirebaseAnalytics.getInstance(context),
            crashlytics = FirebaseCrashlytics.getInstance(),
        )
    }
}
