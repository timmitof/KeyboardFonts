package kg.timmitof.keyboard.clipboard.domain.repository

import kg.timmitof.keyboard.clipboard.domain.model.ClipboardBoard
import kotlinx.coroutines.flow.Flow

interface ClipboardRepository {

    fun observeBoard(): Flow<ClipboardBoard>

    suspend fun captureSystemClip()

    suspend fun setPinned(id: Long, isPinned: Boolean)

    suspend fun remove(id: Long)

    suspend fun clearRecent()
}
