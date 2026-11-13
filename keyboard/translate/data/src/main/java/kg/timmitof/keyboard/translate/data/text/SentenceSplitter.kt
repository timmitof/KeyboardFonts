package kg.timmitof.keyboard.translate.data.text

/**
 * Режет текст на предложения вместе с пробелами после них: склеенные куски дают исходный текст.
 * Граница — знак конца предложения перед пробелом или перевод строки. Сокращения («т. е.») тоже режутся,
 * для перевода чата это не страшно.
 */
internal object SentenceSplitter {

    fun split(text: String): List<String> {
        val pieces = mutableListOf<String>()
        var start = 0
        var index = 0

        while (index < text.length) {
            if (!endsSentence(text, index)) {
                index++
                continue
            }
            var end = index + 1
            while (end < text.length && text[end].isWhitespace()) end++
            pieces += text.substring(start, end)
            start = end
            index = end
        }
        if (start < text.length) pieces += text.substring(start)
        return pieces
    }

    private fun endsSentence(text: String, index: Int): Boolean {
        val char = text[index]
        if (char == '\n') return true
        return char in TERMINATORS && text.getOrNull(index + 1)?.isWhitespace() == true
    }

    private const val TERMINATORS = ".!?…。！？"
}
