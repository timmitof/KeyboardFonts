package kg.timmitof.feature_settings.domain.model

import kg.timmitof.core.billing.domain.model.billingCatalog

/**
 * Уровни чаевых. ID — как в Play Console (расходуемые товары), цены задаются только там.
 * Порядок записей — порядок на экране. Эмодзи и названия — в presentation.
 */
enum class Tip(val productId: String) {
    COFFEE("tip_coffee_99"),
    DONUT("tip_donut_199"),
    PIZZA("tip_pizza_299"),
    BOOST("tip_boost_499"),
    THANKS("tip_thanks_999");

    companion object {
        fun byProductId(id: String): Tip? = entries.firstOrNull { it.productId == id }

        /** Каталог для магазина: все чаевые — расходуемые, их можно отправлять снова и снова. */
        val catalog = billingCatalog { entries.forEach { consumable(it.productId) } }
    }
}

/** Уровень чаевых с ценой из Play — уже в валюте и формате страны пользователя. */
data class TipOffer(
    val tip: Tip,
    val formattedPrice: String,
)
