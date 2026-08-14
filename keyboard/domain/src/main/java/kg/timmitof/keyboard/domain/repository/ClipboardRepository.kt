package kg.timmitof.keyboard.domain.repository

import kg.timmitof.keyboard.domain.model.ClipboardBoard
import kotlinx.coroutines.flow.Flow

/**
 * Буфер обмена клавиатуры: своя история поверх системного буфера.
 */
interface ClipboardRepository {

    fun observeBoard(): Flow<ClipboardBoard>

    suspend fun captureSystemClip()

    suspend fun setPinned(id: Long, isPinned: Boolean)

    suspend fun remove(id: Long)

    /** Чистит незакреплённое — закреплённые карточки остаются. */
    suspend fun clearRecent()
}
