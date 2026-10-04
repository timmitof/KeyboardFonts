package kg.timmitof.feature_splash.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kg.timmitof.feature_splash.data.interactor.SplashInteractorImpl
import kg.timmitof.feature_splash.domain.interactor.SplashInteractor

@Module
@InstallIn(SingletonComponent::class)
abstract class InteractorModule {

    @Binds
    abstract fun bindSplashInteractor(impl: SplashInteractorImpl): SplashInteractor
}
