package kg.timmitof.keyboard.data.di

import kg.timmitof.keyboard.data.AssetTextLoader
import kg.timmitof.keyboard.data.AssetTextLoaderImpl
import kg.timmitof.keyboard.data.settings.BackgroundPhotoStorage
import kg.timmitof.keyboard.data.settings.BackgroundPhotoStorageImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformKeyboardDataModule: Module = module {

    singleOf(::AssetTextLoaderImpl) bind AssetTextLoader::class

    singleOf(::BackgroundPhotoStorageImpl) bind BackgroundPhotoStorage::class
}
