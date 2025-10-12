package kg.timmitof.core.ui.base

/**
 * Базовый тип состояния экрана в MVI-архитектуре.
 *
 * Служит основой для всех `UiState`, описывающих данные и статус UI.
 * Обновляется только во [ViewModel], а UI — лишь наблюдает за изменениями.
 *
 * Пример:
 * ```
 * data class MyState(
 *     val someState: Boolean = false,
 *     val error: String? = null
 * ) : BaseState()
 * ```
 */
abstract class BaseState