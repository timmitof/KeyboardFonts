package kg.timmitof.keyboard.translate.domain.repository

import kg.timmitof.keyboard.translate.domain.model.TranslationModels
import kotlinx.coroutines.flow.Flow

/**
 * Загрузка моделей в фоне: переживает закрытие клавиатуры и приложения, повторяется при обрыве
 * и показывает уведомление с прогрессом (если пользователь разрешил уведомления).
 */
interface TranslationDownloadRepository {

    /** Скачанные и стоящие в очереди модели. */
    fun observeModels(): Flow<TranslationModels>

    /**
     * Языки, которые переводчик качает сам: язык системы (и английский — он встроен),
     * а у английской системы — русский.
     */
    fun defaultCodes(): List<String>

    /** Тихая загрузка языков по умолчанию: только по Wi-Fi; всё уже скачано или в очереди — ничего не делает. */
    suspend fun scheduleDefaults()

    /** Переводчик выключили — ждущая Wi-Fi загрузка по умолчанию больше не нужна. */
    fun cancelDefaults()

    /** Пользователь сам попросил скачать: по любой сети и сразу. Снимает отметку «удалён пользователем». */
    suspend fun download(codes: Collection<String>)

    /**
     * Удаляет скачанный язык и запоминает это: фоновая загрузка по умолчанию его обратно не скачает.
     * @return удалось ли; встроенный язык не удаляется.
     */
    suspend fun delete(code: String): Boolean
}
