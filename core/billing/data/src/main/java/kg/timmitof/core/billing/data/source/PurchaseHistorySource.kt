package kg.timmitof.core.billing.data.source

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.billingHistory by preferencesDataStore(name = "billing_history")

/** Употреблённые покупки Play больше не отдаёт — поэтому оплаченные товары помним сами. */
@Singleton
class PurchaseHistorySource @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun observe(): Flow<Set<String>> = context.billingHistory.data
        .map { it[PURCHASED].orEmpty() }
        .distinctUntilChanged()

    suspend fun add(productIds: Collection<String>) {
        context.billingHistory.edit { it[PURCHASED] = it[PURCHASED].orEmpty() + productIds }
    }

    private companion object {
        val PURCHASED = stringSetPreferencesKey("purchased_products")
    }
}
