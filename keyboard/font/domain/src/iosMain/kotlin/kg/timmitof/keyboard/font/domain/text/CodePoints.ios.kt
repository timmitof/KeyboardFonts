package kg.timmitof.keyboard.font.domain.text

import platform.Foundation.NSCharacterSet

// Наборы Foundation работают с полными кодовыми точками Unicode, как Character на JVM.
// В letterCharacterSet входят и знаки (M*) — их, как Character.isLetter, не считаем буквами.

actual fun isLetterOrDigit(codePoint: Int): Boolean {
    val character = codePoint.toUInt()
    if (NSCharacterSet.decimalDigitCharacterSet.longCharacterIsMember(character)) return true
    return NSCharacterSet.letterCharacterSet.longCharacterIsMember(character) &&
        !NSCharacterSet.nonBaseCharacterSet.longCharacterIsMember(character)
}

actual fun isUpperCase(codePoint: Int): Boolean =
    NSCharacterSet.uppercaseLetterCharacterSet.longCharacterIsMember(codePoint.toUInt())
