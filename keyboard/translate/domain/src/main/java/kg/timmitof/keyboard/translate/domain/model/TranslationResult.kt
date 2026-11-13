package kg.timmitof.keyboard.translate.domain.model

sealed interface TranslationResult {

    data class Success(val text: String) : TranslationResult

    /** Модели пары нет на телефоне: перевод работает офлайн, но язык нужно один раз скачать. */
    data object ModelMissing : TranslationResult

    data object Failed : TranslationResult
}
