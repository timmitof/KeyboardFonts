package kg.timmitof.keyboard.presentation.screens.keyboard.states

data class KeyboardFieldContext(
    val type: KeyboardFieldType = KeyboardFieldType.TEXT,
    val enterAction: EnterAction = EnterAction.RETURN,
    val isMultiLine: Boolean = false,
)
