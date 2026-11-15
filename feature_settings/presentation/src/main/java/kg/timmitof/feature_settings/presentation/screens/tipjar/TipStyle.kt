package kg.timmitof.feature_settings.presentation.screens.tipjar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kg.timmitof.core.billing.domain.model.BillingFailure
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.feature_settings.domain.model.Tip
import kg.timmitof.feature_settings.presentation.R

internal enum class TipStyle(
    val tip: Tip,
    @param:DrawableRes val iconRes: Int,
    val role: AccentRole,
    @param:StringRes val titleRes: Int,
    @param:StringRes val thanksRes: Int,
) {
    COFFEE(Tip.COFFEE, R.drawable.ic_tip_coffee, AccentRole.HINT, R.string.tipjar_coffee, R.string.tipjar_thanks_coffee),
    DONUT(Tip.DONUT, R.drawable.ic_tip_donut, AccentRole.APPEARANCE, R.string.tipjar_donut, R.string.tipjar_thanks_donut),
    PIZZA(Tip.PIZZA, R.drawable.ic_tip_pizza, AccentRole.HINT, R.string.tipjar_pizza, R.string.tipjar_thanks_pizza),
    BOOST(Tip.BOOST, R.drawable.ic_tip_rocket, AccentRole.BRAND, R.string.tipjar_boost, R.string.tipjar_thanks_boost),
    THANKS(Tip.THANKS, R.drawable.ic_tip_heart, AccentRole.APPEARANCE, R.string.tipjar_thanks, R.string.tipjar_thanks_thanks);
}

internal val Tip.style: TipStyle get() = TipStyle.entries.first { it.tip == this }

@get:StringRes
internal val BillingFailure.messageRes: Int
    get() = when (this) {
        BillingFailure.OFFLINE -> R.string.tipjar_error_offline
        BillingFailure.UNAVAILABLE -> R.string.tipjar_error_unavailable
        BillingFailure.PRODUCT_UNAVAILABLE -> R.string.tipjar_error_products
        BillingFailure.ERROR -> R.string.tipjar_error_generic
    }

internal val BillingFailure.canRetry: Boolean get() = this != BillingFailure.UNAVAILABLE
