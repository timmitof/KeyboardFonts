package kg.timmitof.keyboard.data.di

import kg.timmitof.keyboard.data.AssetTextLoader
import kg.timmitof.keyboard.data.IosAssetTextLoader
import kg.timmitof.keyboard.data.settings.BackgroundPhotoStorage
import kg.timmitof.keyboard.data.settings.IosBackgroundPhotoStorage
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformKeyboardDataModule: Module = module {

    singleOf(::IosAssetTextLoader) bind AssetTextLoader::class

    singleOf(::IosBackgroundPhotoStorage) bind BackgroundPhotoStorage::class
}
