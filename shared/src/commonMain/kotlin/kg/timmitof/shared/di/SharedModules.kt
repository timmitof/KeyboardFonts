package kg.timmitof.shared.di

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
import org.koin.core.module.Module

/**
 * Граф приложения для обеих платформ. На Android его же использует сервис клавиатуры — он живёт в том же процессе;
 * клавиатура iOS — отдельный процесс расширения, и граф она поднимает сама.
 */
val sharedModules: List<Module> = listOf(
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
