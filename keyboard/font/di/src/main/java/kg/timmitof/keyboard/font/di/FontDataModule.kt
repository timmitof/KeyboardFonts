package kg.timmitof.keyboard.font.di

import kg.timmitof.keyboard.font.data.FontPanelDataSource
import kg.timmitof.keyboard.font.data.SelectedFontDataSource
import kg.timmitof.keyboard.font.data.repository.FontRepositoryImpl
import kg.timmitof.keyboard.font.domain.repository.FontRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val fontDataModule = module {

    singleOf(::FontRepositoryImpl) bind FontRepository::class

    singleOf(::SelectedFontDataSource)
    singleOf(::FontPanelDataSource)
}
