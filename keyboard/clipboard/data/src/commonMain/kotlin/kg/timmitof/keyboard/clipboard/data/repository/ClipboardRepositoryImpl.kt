package kg.timmitof.keyboard.clipboard.data.repository

import kg.timmitof.core.data.local.dao.ClipboardDao
import kg.timmitof.core.data.local.entities.ClipboardEntryEntity
import kg.timmitof.keyboard.clipboard.data.SystemClipboardSource
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardBoard
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardEntry
import kg.timmitof.keyboard.clipboard.domain.repository.ClipboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.concurrent.Volatile
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ClipboardRepositoryImpl(
    private val clipboardDao: ClipboardDao,
    private val systemClipboard: SystemClipboardSource,
) : ClipboardRepository {

    /** Последний захваченный текст: системный клип не меняется между открытиями клавиатуры, писать в БД заново незачем. */
    @Volatile
    private var lastCaptured: String? = null

    override fun observeBoard(): Flow<ClipboardBoard> = clipboardDao.observeAll()
        .map(List<ClipboardEntryEntity>::toBoard)
        .distinctUntilChanged()

    @OptIn(ExperimentalTime::class)
    override suspend fun captureSystemClip() {
        val text = systemClipboard.read() ?: return
        if (text == lastCaptured) return

        clipboardDao.capture(
            text = text,
            copiedAt = Clock.System.now().toEpochMilliseconds(),
            recentLimit = MAX_RECENT,
        )
        lastCaptured = text
    }

    override suspend fun setPinned(id: Long, isPinned: Boolean) =
        clipboardDao.setPinned(id, isPinned)

    override suspend fun remove(id: Long) = clipboardDao.delete(id)

    override suspend fun clearRecent() = clipboardDao.deleteRecent()

    private companion object {
        const val MAX_RECENT = 20
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
