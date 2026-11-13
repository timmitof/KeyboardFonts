package kg.timmitof.keyboard.clipboard.domain.repository

import kg.timmitof.keyboard.clipboard.domain.model.ClipboardBoard
import kg.timmitof.keyboard.clipboard.domain.model.FreshClip
import kotlinx.coroutines.flow.Flow

interface ClipboardRepository {

    fun observeBoard(): Flow<ClipboardBoard>

    suspend fun captureSystemClip()

    /**
     * Только что скопированный текст (не старше нескольких минут) — для перевода скопированного.
     * Пароли и старые копии не возвращаются; содержимое буфера читается, только если копия свежая.
     */
    suspend fun getFreshClip(): FreshClip?

    suspend fun setPinned(id: Long, isPinned: Boolean)

    suspend fun remove(id: Long)

    suspend fun clearRecent()
}
