package kg.timmitof.feature_home.di

import kg.timmitof.feature_home.data.interactor.HomeInteractorImpl
import kg.timmitof.feature_home.domain.interactor.HomeInteractor
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val homeInteractorModule = module {

    factoryOf(::HomeInteractorImpl) bind HomeInteractor::class
}
