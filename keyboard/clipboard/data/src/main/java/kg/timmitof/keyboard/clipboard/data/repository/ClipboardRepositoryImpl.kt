package kg.timmitof.keyboard.clipboard.data.repository

import kg.timmitof.core.data.local.dao.ClipboardDao
import kg.timmitof.core.data.local.entities.ClipboardEntryEntity
import kg.timmitof.keyboard.clipboard.data.SystemClipboardSource
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardBoard
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardEntry
import kg.timmitof.keyboard.clipboard.domain.model.FreshClip
import kg.timmitof.keyboard.clipboard.domain.repository.ClipboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClipboardRepositoryImpl @Inject constructor(
    private val clipboardDao: ClipboardDao,
    private val systemClipboard: SystemClipboardSource,
) : ClipboardRepository {

    /** Последний захваченный текст: системный клип не меняется между открытиями клавиатуры, писать в БД заново незачем. */
    @Volatile
    private var lastCaptured: String? = null

    override fun observeBoard(): Flow<ClipboardBoard> = clipboardDao.observeAll()
        .map(List<ClipboardEntryEntity>::toBoard)
        .distinctUntilChanged()

    override suspend fun captureSystemClip() {
        val text = systemClipboard.read() ?: return
        if (text == lastCaptured) return

        clipboardDao.capture(
            text = text,
            copiedAt = System.currentTimeMillis(),
            recentLimit = MAX_RECENT,
        )
        lastCaptured = text
    }

    /** Метка копирования в описании буфера — в шкале `System.currentTimeMillis()`, сравниваем в ней же. */
    override suspend fun getFreshClip(): FreshClip? =
        systemClipboard.readFresh(FRESH_CLIP_MILLIS, System.currentTimeMillis())
            ?.let { (text, copiedAt) -> FreshClip(text, copiedAt) }

    override suspend fun setPinned(id: Long, isPinned: Boolean) =
        clipboardDao.setPinned(id, isPinned)

    override suspend fun remove(id: Long) = clipboardDao.delete(id)

    override suspend fun clearRecent() = clipboardDao.deleteRecent()

    private companion object {
        const val MAX_RECENT = 20

        /** Перевод предлагаем только только что скопированному: старый текст в буфере — уже не «входящее». */
        const val FRESH_CLIP_MILLIS = 3 * 60 * 1000L
    }
}

private fun List<ClipboardEntryEntity>.toBoard(): ClipboardBoard {
    val (pinned, recent) = map(ClipboardEntryEntity::toDomain).partition(ClipboardEntry::isPinned)

    return ClipboardBoard(pinned = pinned, recent = recent)
}

private fun ClipboardEntryEntity.toDomain() = ClipboardEntry(
    id = id,
    text = text,
    isPinned = isPinned,
    copiedAt = copiedAt,
)
