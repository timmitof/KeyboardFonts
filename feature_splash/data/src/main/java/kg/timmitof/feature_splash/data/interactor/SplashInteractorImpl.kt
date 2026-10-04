package kg.timmitof.feature_splash.data.interactor

import kg.timmitof.feature_splash.domain.interactor.SplashInteractor
import kg.timmitof.feature_splash.domain.repository.TemplateRepository
import kg.timmitof.keyboard.integration.KeyboardContract
import javax.inject.Inject

class SplashInteractorImpl @Inject constructor(
    private val templateRepository: TemplateRepository,
    private val keyboardContract: KeyboardContract,
) : SplashInteractor {

    override suspend fun loadTemplates() {
        if (templateRepository.getTemplateCount() != 0) return
        templateRepository.loadTemplatesFromAsset()
    }

    override fun isKeyboardReady(): Boolean {
        val state = keyboardContract.getKeyboardState()
        return state.isEnabled && state.isSelected
    }
}
