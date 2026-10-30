package kg.timmitof.keyboardfonts.di

import kg.timmitof.core.data.di.storageModule
import kg.timmitof.feature_home.di.homeInteractorModule
import kg.timmitof.feature_home.presentation.di.homePresentationModule
import kg.timmitof.feature_settings.di.settingsInteractorModule
import kg.timmitof.feature_settings.presentation.di.settingsPresentationModule
import kg.timmitof.feature_splash.di.splashInteractorModule
import kg.timmitof.feature_splash.presentation.di.splashPresentationModule
import kg.timmitof.keyboard.clipboard.di.clipboardDataModule
import kg.timmitof.keyboard.di.keyboardDataModule
import kg.timmitof.keyboard.font.di.fontDataModule
import kg.timmitof.keyboard.integration.di.keyboardIntegrationModule
import kg.timmitof.keyboard.suggestion.di.suggestionDataModule

/** Граф приложения: его же использует сервис клавиатуры — он живёт в том же процессе. */
internal val appModules = listOf(
    storageModule,
    keyboardIntegrationModule,
    keyboardDataModule,
    fontDataModule,
    suggestionDataModule,
    clipboardDataModule,
    splashInteractorModule,
    homeInteractorModule,
    settingsInteractorModule,
    splashPresentationModule,
    homePresentationModule,
    settingsPresentationModule,
)
