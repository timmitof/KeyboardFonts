package kg.timmitof.feature_settings.domain.model

import kg.timmitof.core.billing.domain.model.billingCatalog

/** ID — как в Play Console. Порядок записей — порядок на экране. */
enum class Tip(val productId: String) {
    COFFEE("tip_coffee_99"),
    DONUT("tip_donut_199"),
    PIZZA("tip_pizza_299"),
    BOOST("tip_boost_499"),
    THANKS("tip_thanks_999");

    companion object {
        fun byProductId(id: String): Tip? = entries.firstOrNull { it.productId == id }

        val catalog = billingCatalog { entries.forEach { consumable(it.productId) } }
    }
}

data class TipOffer(
    val tip: Tip,
    val formattedPrice: String,
)
