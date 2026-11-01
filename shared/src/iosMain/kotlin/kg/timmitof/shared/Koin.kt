package kg.timmitof.shared

import kg.timmitof.shared.di.sharedModules
import org.koin.core.context.startKoin

/** Вызывается один раз при старте приложения, до первого экрана (в Swift — `KoinKt.doInitKoin()`). */
@Suppress("unused")
fun initKoin() {
    startKoin { modules(sharedModules) }
}
