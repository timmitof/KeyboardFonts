package kg.timmitof.keyboard.suggestion.domain.repository

import kg.timmitof.keyboard.suggestion.domain.model.TypingEvent

/**
 * Счётчики качества Т9 — чтобы настраивать веса движка по цифрам, а не на ощущениях.
 * Ведутся только в отладочной сборке и не покидают устройство.
 */
interface TypingStatsRepository {

    fun record(event: TypingEvent)

    /** Сохраняет счётчики и пишет сводку в лог (тег `TypingStats`). */
    suspend fun flush()
}
