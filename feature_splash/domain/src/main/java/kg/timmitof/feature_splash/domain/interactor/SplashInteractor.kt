package kg.timmitof.feature_splash.domain.interactor

interface SplashInteractor {

    /** Загрузить шаблоны из ассетов — только при первом запуске, пока база пуста. */
    suspend fun loadTemplates()

    /** Клавиатура подключена — можно сразу в настройки, минуя подключение. */
    fun isKeyboardReady(): Boolean
}
