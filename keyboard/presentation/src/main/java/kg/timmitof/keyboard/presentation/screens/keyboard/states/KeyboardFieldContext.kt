package kg.timmitof.keyboard.presentation.screens.keyboard.states

/** @param packageName приложение, в котором набирают: переводчик помнит свою пару для каждого. */
data class KeyboardFieldContext(
    val type: KeyboardFieldType = KeyboardFieldType.TEXT,
    val enterAction: EnterAction = EnterAction.RETURN,
    val isMultiLine: Boolean = false,
    val packageName: String? = null,
)
