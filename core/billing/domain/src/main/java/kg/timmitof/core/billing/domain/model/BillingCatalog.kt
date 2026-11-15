package kg.timmitof.core.billing.domain.model

/** Как обращаться с покупкой после оплаты. */
enum class BillingProductKind {
    /** Расходуемый (чаевые) */
    CONSUMABLE,
}

/** Товар, о котором знает приложение. Покупки чужих товаров репозиторий не трогает. */
data class BillingProductSpec(
    val id: String,
    val kind: BillingProductKind,
)

@DslMarker
annotation class BillingCatalogDsl

/**
 * Сборщик списка товаров:
 * ```
 * billingCatalog { consumable("tip_coffee_99") }
 * ```
 * Фича отдаёт свой список в граф Hilt (`@ElementsIntoSet`), а репозиторий собирает их все.
 */
@BillingCatalogDsl
class BillingCatalogBuilder internal constructor() {

    private val specs = linkedSetOf<BillingProductSpec>()

    fun consumable(id: String) {
        specs += BillingProductSpec(id, BillingProductKind.CONSUMABLE)
    }

    internal fun build(): Set<BillingProductSpec> = specs
}

fun billingCatalog(block: BillingCatalogBuilder.() -> Unit): Set<BillingProductSpec> =
    BillingCatalogBuilder().apply(block).build()
