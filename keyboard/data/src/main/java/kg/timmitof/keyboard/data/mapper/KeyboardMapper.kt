package kg.timmitof.keyboard.data.mapper

import kg.timmitof.keyboard.data.models.KeyLongPressDto
import kg.timmitof.keyboard.data.models.KeyboardKeyDto
import kg.timmitof.keyboard.data.models.KeyboardLayoutDto
import kg.timmitof.keyboard.domain.model.KeyLongPressType
import kg.timmitof.keyboard.domain.model.KeyType
import kg.timmitof.keyboard.domain.model.KeyboardKey
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.domain.model.LongPressAction
import kg.timmitof.keyboard.domain.model.LongPressCharacter

object KeyboardMapper {

    fun KeyboardLayoutDto.toDomain(): KeyboardLayout = KeyboardLayout(
        name = name,
        rows = rows.map { row -> row.mapNotNull { it.toDomain() } }
    )

    fun KeyboardKeyDto.toDomain(): KeyboardKey? {
        val keyType = KeyType.fromString(type) ?: return null  // неизвестный тип — игнор

        return when (keyType) {
            KeyType.CHARACTER    -> KeyboardKey.Character(
                weight = weight,
                labelLower = labelLower.orEmpty(),
                labelUpper = labelUpper.orEmpty(),
                longPress = longPress?.toDomain()
            )
            KeyType.SPACE              -> KeyboardKey.Space(weight)
            KeyType.ENTER              -> KeyboardKey.Enter(weight)
            KeyType.SHIFT              -> KeyboardKey.Shift(weight)
            KeyType.BACKSPACE          -> KeyboardKey.Backspace(weight)
            KeyType.SYMBOLS_SWITCH     -> KeyboardKey.SymbolsSwitch(weight)
            KeyType.SYMBOLS_ALT_SWITCH -> KeyboardKey.SymbolsAltSwitch(weight, label = labelLower.orEmpty())
            KeyType.ABC_SWITCH         -> KeyboardKey.AbcSwitch(weight)
            KeyType.EMOJI_SWITCH       -> KeyboardKey.EmojiSwitch(weight)
            KeyType.SPACER             -> KeyboardKey.Spacer(weight)
        }
    }

    fun KeyLongPressDto.toDomain(): LongPressAction? {
        val longPressType = KeyLongPressType.fromString(type) ?: return null

        return when (longPressType) {
            KeyLongPressType.SYMBOLS    -> toSymbols()
            KeyLongPressType.MICROPHONE -> LongPressAction.Microphone
        }
    }

    private fun KeyLongPressDto.toSymbols(): LongPressAction.Symbols? {
        val options = symbols.orEmpty().map { LongPressCharacter(it) } +
            characters.orEmpty().map { LongPressCharacter(it.labelLower, it.labelUpper ?: it.labelLower) }

        return options.takeIf { it.isNotEmpty() }?.let { LongPressAction.Symbols(it) }
    }
}