package kg.timmitof.keyboard.translate.data.source

/** Движок перевода. Отдельный интерфейс, чтобы логику репозитория проверять без ML Kit. */
interface TranslatorEngine {

    /** Модели пары должны быть уже на телефоне. Бросает исключение, если перевести не вышло. */
    suspend fun translate(text: String, sourceCode: String, targetCode: String): String

    /** Отпускает модель из памяти; следующий перевод загрузит её заново. */
    fun close()
}
