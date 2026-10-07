package kg.timmitof.feature_home.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kg.timmitof.feature_home.data.interactor.HomeInteractorImpl
import kg.timmitof.feature_home.domain.interactor.HomeInteractor

@Module
@InstallIn(SingletonComponent::class)
abstract class InteractorModule {

    @Binds
    abstract fun bindHomeInteractor(impl: HomeInteractorImpl): HomeInteractor
}
