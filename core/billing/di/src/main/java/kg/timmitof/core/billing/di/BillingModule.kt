package kg.timmitof.core.billing.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import kg.timmitof.core.billing.data.repository.BillingRepositoryImpl
import kg.timmitof.core.billing.domain.model.BillingProductSpec
import kg.timmitof.core.billing.domain.repository.BillingRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {

    @Binds
    @Singleton
    abstract fun bindBillingRepository(impl: BillingRepositoryImpl): BillingRepository

    /** Каталог товаров собирается из фич (`@ElementsIntoSet` + [kg.timmitof.core.billing.domain.model.billingCatalog]); может быть пустым. */
    @Multibinds
    abstract fun billingCatalog(): Set<BillingProductSpec>
}
