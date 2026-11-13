package kg.timmitof.keyboard.translate.data

import kotlin.coroutines.cancellation.CancellationException

/**
 * Ошибку ML Kit превращаем в null, а отмену корутины пробрасываем: иначе устаревший перевод
 * (пользователь уже набрал дальше) доедет до поля.
 */
internal inline fun <T> runCatchingCancellable(block: () -> T): T? = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    null
}
