package kg.timmitof.keyboardfonts

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kg.timmitof.core.analytics.Analytics
import kg.timmitof.keyboardfonts.analytics.AnalyticsUserProperties
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

@HiltAndroidApp
class KeyboardFontsApp : Application() {

    @Inject
    lateinit var analytics: Analytics

    @Inject
    internal lateinit var userProperties: AnalyticsUserProperties

    override fun onCreate() {
        super.onCreate()

        // Процесс общий у приложения и клавиатуры: свойства пользователя обновляются, кто бы ни поменял настройки.
        val errorHandler = CoroutineExceptionHandler { _, throwable -> analytics.recordError(throwable) }
        userProperties.start(CoroutineScope(SupervisorJob() + Dispatchers.Default + errorHandler))
    }
}
