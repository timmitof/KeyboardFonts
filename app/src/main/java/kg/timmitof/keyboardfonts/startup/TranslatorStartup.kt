package kg.timmitof.keyboardfonts.startup

import kg.timmitof.keyboard.domain.model.KeyboardFeature
import kg.timmitof.keyboard.domain.repository.FeatureAccessRepository
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslationDownloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Пока переводчик включён и открыт пользователю, языки по умолчанию качаются в фоне по Wi-Fi.
 * Живёт в процессе приложения, а клавиатура — в нём же, поэтому одного запуска на процесс хватает.
 */
@Singleton
class TranslatorStartup @Inject constructor(
    private val settingsRepository: KeyboardSettingsRepository,
    private val featureAccessRepository: FeatureAccessRepository,
    private val downloadRepository: TranslationDownloadRepository,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        combine(
            settingsRepository.observeSettings(),
            featureAccessRepository.observeAccess(),
        ) { settings, access -> settings.isActive(KeyboardFeature.TRANSLATOR, access) }
            .distinctUntilChanged()
            .onEach { isActive ->
                if (isActive) downloadRepository.scheduleDefaults() else downloadRepository.cancelDefaults()
            }
            .launchIn(scope)
    }
}
