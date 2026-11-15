package kg.timmitof.core.billing.domain.model

enum class BillingProductKind {
    /** Расходуемый (чаевые) */
    CONSUMABLE,
}

data class BillingProductSpec(
    val id: String,
    val kind: BillingProductKind,
)

@DslMarker
annotation class BillingCatalogDsl

/**
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
