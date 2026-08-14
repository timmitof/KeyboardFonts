package kg.timmitof.feature_settings.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kg.timmitof.feature_settings.domain.interactor.SettingsInteractor
import kg.timmitof.feature_settings.domain.usecase.GetSettingsSummaryUseCase
import kg.timmitof.feature_settings.domain.usecase.ObserveKeyboardSettingsUseCase
import kg.timmitof.feature_settings.domain.usecase.SetKeyboardThemeUseCase
import kg.timmitof.feature_settings.domain.usecase.SetKeyboardToggleUseCase
import kg.timmitof.keyboard.font.domain.repository.FontRepository
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import kg.timmitof.keyboard.integration.KeyboardContract

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    fun provideObserveKeyboardSettingsUseCase(
        keyboardSettingsRepository: KeyboardSettingsRepository
    ): ObserveKeyboardSettingsUseCase {
        return ObserveKeyboardSettingsUseCase(keyboardSettingsRepository)
    }

    @Provides
    fun provideSetKeyboardToggleUseCase(
        keyboardSettingsRepository: KeyboardSettingsRepository
    ): SetKeyboardToggleUseCase {
        return SetKeyboardToggleUseCase(keyboardSettingsRepository)
    }

    @Provides
    fun provideSetKeyboardThemeUseCase(
        keyboardSettingsRepository: KeyboardSettingsRepository
    ): SetKeyboardThemeUseCase {
        return SetKeyboardThemeUseCase(keyboardSettingsRepository)
    }

    @Provides
    fun provideGetSettingsSummaryUseCase(
        languageRepository: LanguageRepository,
        fontRepository: FontRepository,
        keyboardContract: KeyboardContract,
    ): GetSettingsSummaryUseCase {
        return GetSettingsSummaryUseCase(
            languageRepository = languageRepository,
            fontRepository = fontRepository,
            keyboardContract = keyboardContract,
        )
    }

    @Provides
    fun provideSettingsInteractor(
        observeKeyboardSettingsUseCase: ObserveKeyboardSettingsUseCase,
        setKeyboardToggleUseCase: SetKeyboardToggleUseCase,
        setKeyboardThemeUseCase: SetKeyboardThemeUseCase,
        getSettingsSummaryUseCase: GetSettingsSummaryUseCase,
    ): SettingsInteractor {
        return SettingsInteractor(
            observeKeyboardSettingsUseCase = observeKeyboardSettingsUseCase,
            setKeyboardToggleUseCase = setKeyboardToggleUseCase,
            setKeyboardThemeUseCase = setKeyboardThemeUseCase,
            getSettingsSummaryUseCase = getSettingsSummaryUseCase,
        )
    }
}
