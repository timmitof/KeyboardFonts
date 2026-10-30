package kg.timmitof.keyboard.domain.repository

import kg.timmitof.keyboard.domain.model.KeyboardLanguages
import kotlinx.coroutines.flow.Flow

interface LanguageRepository {

    suspend fun getLanguages(): KeyboardLanguages

    /** Приложение и клавиатура — разные процессы: изменения списка приходят потоком. */
    fun observeLanguages(): Flow<KeyboardLanguages>

    suspend fun setSelectedLanguage(code: String)

    /**
     * Упорядоченный список включённых языков. Коды вне каталога отбрасываются, пустой список игнорируется:
     * без языка клавиатура не работает. Удалили выбранный — выбранным станет первый из списка.
     */
    suspend fun setEnabledLanguages(codes: List<String>)
}
