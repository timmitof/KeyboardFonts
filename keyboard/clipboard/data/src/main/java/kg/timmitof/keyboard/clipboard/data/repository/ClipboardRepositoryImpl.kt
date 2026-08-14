package kg.timmitof.keyboard.clipboard.data.repository

import kg.timmitof.core.data.local.dao.ClipboardDao
import kg.timmitof.core.data.local.entities.ClipboardEntryEntity
import kg.timmitof.keyboard.clipboard.data.SystemClipboardSource
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardBoard
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardEntry
import kg.timmitof.keyboard.clipboard.domain.repository.ClipboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClipboardRepositoryImpl @Inject constructor(
    private val clipboardDao: ClipboardDao,
    private val systemClipboard: SystemClipboardSource,
) : ClipboardRepository {

    override fun observeBoard(): Flow<ClipboardBoard> = clipboardDao.observeAll()
        .map(List<ClipboardEntryEntity>::toBoard)

    override suspend fun captureSystemClip() {
        val text = systemClipboard.read() ?: return

        clipboardDao.capture(
            text = text,
            copiedAt = System.currentTimeMillis(),
            recentLimit = MAX_RECENT,
        )
    }

    override suspend fun setPinned(id: Long, isPinned: Boolean) =
        clipboardDao.setPinned(id, isPinned)

    override suspend fun remove(id: Long) = clipboardDao.delete(id)

    override suspend fun clearRecent() = clipboardDao.deleteRecent()

    private companion object {
        /** Столько незакреплённых записей держим: дальше карточки уже не листают. */
        const val MAX_RECENT = 20
    }
}

/** Список уже отсортирован запросом — остаётся разделить его на секции листа. */
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
