package kg.timmitof.core.analytics

/**
 * Аналитика и отчёты об ошибках. Введённый текст, буфер обмена и эмодзи сюда не попадают никогда —
 * только счётчики, идентификаторы настроек, шрифтов и языков.
 */
interface Analytics {

    fun log(event: AnalyticsEvent)

    /** Срез «чем пользуются»: тема, фон, звук. Имя — до 24 символов, значение — до 36; `null` стирает свойство. */
    fun setUserProperty(name: String, value: String?)

    /** Обработанная ошибка: в Crashlytics уходит как non-fatal. */
    fun recordError(throwable: Throwable)
}

/** Имя — snake_case до 40 символов, параметров до 25, строки до 100 символов: ограничения Firebase. */
data class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any> = emptyMap(),
)

/** Сборка без Firebase и превью. */
object NoOpAnalytics : Analytics {
    override fun log(event: AnalyticsEvent) = Unit
    override fun setUserProperty(name: String, value: String?) = Unit
    override fun recordError(throwable: Throwable) = Unit
}
