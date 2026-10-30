package kg.timmitof.feature_splash.di

import kg.timmitof.feature_splash.data.interactor.SplashInteractorImpl
import kg.timmitof.feature_splash.domain.interactor.SplashInteractor
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val splashInteractorModule = module {

    factoryOf(::SplashInteractorImpl) bind SplashInteractor::class
}
