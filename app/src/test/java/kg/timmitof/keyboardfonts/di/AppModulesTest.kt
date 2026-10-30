package kg.timmitof.keyboardfonts.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

/** Каждый конструктор в графе находит свои зависимости; Context и SavedStateHandle подставляют Android и Koin. */
class AppModulesTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun appModulesAreComplete() {
        module { includes(appModules) }.verify(
            extraTypes = listOf(Context::class, SavedStateHandle::class),
        )
    }
}
