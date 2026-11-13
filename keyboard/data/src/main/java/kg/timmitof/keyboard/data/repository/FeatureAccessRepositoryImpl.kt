package kg.timmitof.keyboard.data.repository

import kg.timmitof.keyboard.domain.model.FeatureAccess
import kg.timmitof.keyboard.domain.repository.FeatureAccessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/** Покупок пока нет — открыто только бесплатное. С биллингом сюда придёт поток купленных уровней. */
class FeatureAccessRepositoryImpl @Inject constructor() : FeatureAccessRepository {

    override fun observeAccess(): Flow<FeatureAccess> = flowOf(FeatureAccess())
}
