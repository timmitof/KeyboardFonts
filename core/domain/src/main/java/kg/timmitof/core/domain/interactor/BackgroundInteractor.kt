package kg.timmitof.core.domain.interactor

import kg.timmitof.core.domain.model.BackgroundModel
import kg.timmitof.core.domain.repository.BackgroundRepository
import javax.inject.Inject

/**
 * Методы для работы с фонами
 */
class BackgroundInteractor @Inject constructor(
    private val backgroundRepository: BackgroundRepository
) {
    suspend fun getAllBackgrounds(): List<BackgroundModel> = backgroundRepository.getBackgrounds()
}