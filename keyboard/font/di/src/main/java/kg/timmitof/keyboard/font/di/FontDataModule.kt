package kg.timmitof.keyboard.font.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kg.timmitof.keyboard.font.data.repository.FontRepositoryImpl
import kg.timmitof.keyboard.font.domain.repository.FontRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FontDataModule {

    @Binds
    @Singleton
    abstract fun bindFontRepository(impl: FontRepositoryImpl): FontRepository
}
