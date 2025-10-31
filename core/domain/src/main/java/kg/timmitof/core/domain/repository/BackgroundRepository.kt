package kg.timmitof.core.domain.repository

import kg.timmitof.core.domain.model.BackgroundModel

interface BackgroundRepository {

    suspend fun getBackgrounds(): List<BackgroundModel>
}