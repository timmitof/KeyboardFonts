package kg.timmitof.feature_splash.data.interactor

import kg.timmitof.feature_splash.domain.interactor.SplashInteractor
import kg.timmitof.keyboard.integration.KeyboardContract
import javax.inject.Inject

class SplashInteractorImpl @Inject constructor(
    private val keyboardContract: KeyboardContract,
) : SplashInteractor {

    override fun isKeyboardReady(): Boolean {
        val state = keyboardContract.getKeyboardState()
        return state.isEnabled && state.isSelected
    }
}
