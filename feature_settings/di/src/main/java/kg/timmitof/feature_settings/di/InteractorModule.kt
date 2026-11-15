package kg.timmitof.feature_settings.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import kg.timmitof.core.billing.domain.model.BillingProductSpec
import kg.timmitof.feature_settings.data.interactor.SettingsInteractorImpl
import kg.timmitof.feature_settings.data.interactor.TipJarInteractorImpl
import kg.timmitof.feature_settings.domain.interactor.SettingsInteractor
import kg.timmitof.feature_settings.domain.interactor.TipJarInteractor
import kg.timmitof.feature_settings.domain.model.Tip

@Module
@InstallIn(SingletonComponent::class)
abstract class InteractorModule {

    @Binds
    abstract fun bindSettingsInteractor(impl: SettingsInteractorImpl): SettingsInteractor

    @Binds
    abstract fun bindTipJarInteractor(impl: TipJarInteractorImpl): TipJarInteractor

    companion object {
        /** Чаевые — в общий каталог магазина: без этого репозиторий не станет употреблять их покупки. */
        @Provides
        @ElementsIntoSet
        fun tipCatalog(): Set<BillingProductSpec> = Tip.catalog
    }
}
