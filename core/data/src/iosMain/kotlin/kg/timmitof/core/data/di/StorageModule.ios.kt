package kg.timmitof.core.data.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kg.timmitof.core.data.AppDirectory
import kg.timmitof.core.data.local.AppDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

// TODO: клавиатуре нужны те же файлы, что приложению, — перенести в контейнер App Group, когда появится iosApp.
internal actual val platformStorageModule: Module = module {

    single { AppDirectory(documentDirectory()) }

    single {
        Room.databaseBuilder<AppDatabase>(name = "${get<AppDirectory>().path}/${AppDatabase.TAG}")
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val url: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return requireNotNull(url?.path) { "Documents directory is unavailable" }
}
