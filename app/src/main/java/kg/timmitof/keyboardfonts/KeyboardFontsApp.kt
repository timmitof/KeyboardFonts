package kg.timmitof.keyboardfonts

import android.app.Application
import kg.timmitof.keyboardfonts.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class KeyboardFontsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@KeyboardFontsApp)
            modules(appModules)
        }
    }
}
