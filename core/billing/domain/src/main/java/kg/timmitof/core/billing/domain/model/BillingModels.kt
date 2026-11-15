package kg.timmitof.core.billing.domain.model

/** Товар магазина, как его отдал Play: цена — уже локализованная строка («99 ₽», «1 290 ₸»), её не собираем сами. */
data class BillingProduct(
    val id: String,
    val formattedPrice: String,
)

/** Почему с магазином не вышло. По причине экран выбирает текст и решает, есть ли смысл в «Повторить». */
enum class BillingFailure {
    /** Нет сети или сервис Play временно не отвечает — стоит повторить. */
    OFFLINE,

    /** Оплата через Play недоступна: регион (например, Россия), нет Play Маркета или аккаунта. Повтор не поможет. */
    UNAVAILABLE,

    /** Товаров нет в консоли (не созданы, не активны) или сборка поставлена не из Play. */
    PRODUCT_UNAVAILABLE,

    /** Прочие ошибки Play. */
    ERROR,
}

/** Подключение к Play Billing. */
sealed interface BillingStatus {
    data object Idle : BillingStatus
    data object Connecting : BillingStatus
    data object Ready : BillingStatus
    data class Failed(val failure: BillingFailure) : BillingStatus
}

/** Результат операции магазина без исключений: ошибки Play — обычное дело (сеть, регион), а не авария. */
sealed interface BillingOutcome<out T> {
    data class Success<T>(val value: T) : BillingOutcome<T>
    data class Failure(val failure: BillingFailure) : BillingOutcome<Nothing>
}

/** Что произошло с покупкой. Приходит потоком [kg.timmitof.core.billing.domain.repository.BillingRepository.purchaseUpdates]. */
sealed interface PurchaseUpdate {

    /** Оплачено и обработано (расходуемый товар употреблён) — можно благодарить. */
    data class Completed(val productIds: List<String>) : PurchaseUpdate

    /** Отложенная оплата (наличные, перевод): денег ещё нет — не благодарим, ждём [Completed]. */
    data class Pending(val productIds: List<String>) : PurchaseUpdate

    /** Пользователь закрыл окно оплаты. */
    data object Cancelled : PurchaseUpdate

    data class Failed(val failure: BillingFailure) : PurchaseUpdate
}
