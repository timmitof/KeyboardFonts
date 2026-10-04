package kg.timmitof.feature_splash.domain.interactor

interface SplashInteractor {

    /** Загрузить шаблоны из ассетов — только при первом запуске, пока база пуста. */
    suspend fun loadTemplates()

    fun isKeyboardReady(): Boolean
}
