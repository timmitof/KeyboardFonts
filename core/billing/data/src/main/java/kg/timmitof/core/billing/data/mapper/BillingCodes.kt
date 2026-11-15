package kg.timmitof.core.billing.data.mapper

import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import kg.timmitof.core.billing.domain.model.BillingFailure
import kg.timmitof.core.billing.domain.model.BillingProduct

internal val BillingResult.isOk: Boolean get() = responseCode == BillingResponseCode.OK

/** BILLING_UNAVAILABLE приходит и там, где Play не принимает оплату. */
internal fun BillingResult.toFailure(): BillingFailure = when (responseCode) {
    BillingResponseCode.SERVICE_UNAVAILABLE,
    BillingResponseCode.NETWORK_ERROR,
    BillingResponseCode.SERVICE_DISCONNECTED -> BillingFailure.OFFLINE
    BillingResponseCode.BILLING_UNAVAILABLE -> BillingFailure.UNAVAILABLE
    BillingResponseCode.ITEM_UNAVAILABLE -> BillingFailure.PRODUCT_UNAVAILABLE
    else -> BillingFailure.ERROR
}

internal val BillingResult.isRetryable: Boolean
    get() = when (responseCode) {
        BillingResponseCode.SERVICE_UNAVAILABLE,
        BillingResponseCode.NETWORK_ERROR,
        BillingResponseCode.SERVICE_DISCONNECTED,
        BillingResponseCode.ERROR -> true
        else -> false
    }

internal fun ProductDetails.toBillingProduct(): BillingProduct? =
    oneTimePurchaseOfferDetails?.formattedPrice?.let { price -> BillingProduct(id = productId, formattedPrice = price) }
