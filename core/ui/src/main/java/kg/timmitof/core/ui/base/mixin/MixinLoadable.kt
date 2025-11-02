package kg.timmitof.core.ui.base.mixin

import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.core.ui.base.BaseViewModel

/**
 * Миксин, добавляющий в [BaseState] стандартный флаг загрузки.
 *
 * Реализуйте его в состоянии экрана, чтобы получить простые хелперы
 * для переключения оверлея загрузки:
 *
 * ```
 * data class MyState(
 *   val items: List<Item> = emptyList(),
 *   override val isLoading: Boolean = false,
 * ) : BaseState(), LoadableMixin {
 *   override fun withLoading(isLoading: Boolean) = copy(isLoading = isLoading)
 * }
 * ```
 *
 * Затем во ViewModel:
 * ```
 * withLoading<MyState> {
 *   repository.fetch() // во время выполнения suspend-блока `isLoading` = true, в finally — false
 * }
 * ```
 */
interface MixinLoadable {
    /** Показывать ли на экране индикатор загрузки. */
    val isLoading: Boolean

    /**
     * Вернуть копию состояния с указанным флагом загрузки.
     * Типичная реализация для состояний-`data class`: `copy(isLoading = isLoading)`.
     */
    fun updateLoadingState(isLoading: Boolean): MixinLoadable

    companion object {

        /**
         * Переключить флаг загрузки внутри цикла Orbit `intent {}` и `reduce {}`.
         *
         * Ограничения:
         * - `S` должен быть вашим состоянием экрана, реализующим и [BaseState], и [MixinLoadable].
         *
         * Пример использования:
         * ```
         * setLoading<MyState>(true)
         * // выполняем что-то не требующее приостановки...
         * setLoading<MyState>(false)
         * ```
         */
        inline fun <reified S> BaseViewModel<S, *, *>.setLoadingState(
            on: Boolean
        ) where S : BaseState, S : MixinLoadable {
            intent {
                @Suppress("UNCHECKED_CAST")
                reduce { (state as MixinLoadable).updateLoadingState(on) as S }
            }
        }

        /**
         * Выполнить suspend-[block], автоматически управляя флагом загрузки.
         *
         * Поведение:
         * - Перед началом [block] устанавливает `isLoading = true`
         * - Всегда устанавливает `isLoading = false` в `finally`, даже если [block] выбросит исключение
         *
         * Типичный пример:
         * ```
         * withLoading<MyState> {
         *   repository.sync() // длительная операция
         * }
         * ```
         */
        suspend inline fun <reified S> BaseViewModel<S, *, *>.runWithLoading(
            block: suspend () -> Unit
        ) where S : BaseState, S : MixinLoadable {
            try {
                setLoadingState(true)
                block()
            } finally {
                setLoadingState(false)
            }
        }
    }
}