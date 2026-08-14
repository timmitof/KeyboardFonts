package kg.timmitof.keyboard.clipboard.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kg.timmitof.keyboard.clipboard.data.repository.ClipboardRepositoryImpl
import kg.timmitof.keyboard.clipboard.domain.repository.ClipboardRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ClipboardDataModule {

    @Binds
    @Singleton
    abstract fun bindClipboardRepository(impl: ClipboardRepositoryImpl): ClipboardRepository
}
