package kg.timmitof.core.billing.domain.model

data class BillingProduct(
    val id: String,
    val formattedPrice: String,
)

enum class BillingFailure {
    OFFLINE,

    /** Регион без оплаты через Play, нет Play Маркета или аккаунта. */
    UNAVAILABLE,

    /** Товаров нет в консоли (не созданы, не активны) или сборка поставлена не из Play. */
    PRODUCT_UNAVAILABLE,

    ERROR,
}

sealed interface BillingStatus {
    data object Idle : BillingStatus
    data object Connecting : BillingStatus
    data object Ready : BillingStatus
    data class Failed(val failure: BillingFailure) : BillingStatus
}

sealed interface BillingOutcome<out T> {
    data class Success<T>(val value: T) : BillingOutcome<T>
    data class Failure(val failure: BillingFailure) : BillingOutcome<Nothing>
}

sealed interface PurchaseUpdate {

    data class Completed(val productIds: List<String>) : PurchaseUpdate

    /** Денег ещё нет — не благодарим, ждём [Completed]. */
    data class Pending(val productIds: List<String>) : PurchaseUpdate

    data object Cancelled : PurchaseUpdate

    data class Failed(val failure: BillingFailure) : PurchaseUpdate
}
