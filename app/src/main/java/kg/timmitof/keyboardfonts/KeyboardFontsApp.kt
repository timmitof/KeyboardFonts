package kg.timmitof.keyboardfonts

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kg.timmitof.keyboardfonts.startup.TranslatorStartup
import javax.inject.Inject

/** WorkManager создаётся по требованию с фабрикой Hilt: воркерам нужны репозитории из графа. */
@HiltAndroidApp
class KeyboardFontsApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var translatorStartup: TranslatorStartup

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        translatorStartup.start()
    }
}
