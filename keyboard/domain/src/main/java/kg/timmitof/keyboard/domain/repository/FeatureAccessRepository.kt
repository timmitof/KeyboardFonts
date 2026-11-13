package kg.timmitof.keyboard.domain.repository

import kg.timmitof.keyboard.domain.model.FeatureAccess
import kotlinx.coroutines.flow.Flow

/** Какие возможности открыты пользователю. Поток: покупка должна открывать фичу сразу, и в клавиатуре тоже. */
interface FeatureAccessRepository {

    fun observeAccess(): Flow<FeatureAccess>
}
