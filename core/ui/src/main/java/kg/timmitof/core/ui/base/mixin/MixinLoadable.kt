package kg.timmitof.core.ui.base.mixin

import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.core.ui.base.BaseViewModel

interface MixinLoadable {
    val isLoading: Boolean

    fun updateLoadingState(isLoading: Boolean): MixinLoadable

    companion object {

        inline fun <reified S> BaseViewModel<S, *, *>.setLoadingState(
            on: Boolean
        ) where S : BaseState, S : MixinLoadable {
            intent {
                @Suppress("UNCHECKED_CAST")
                reduce { (state as MixinLoadable).updateLoadingState(on) as S }
            }
        }

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