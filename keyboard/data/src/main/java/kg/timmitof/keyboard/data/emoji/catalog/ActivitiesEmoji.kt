package kg.timmitof.keyboard.data.emoji.catalog

internal object ActivitiesEmoji {

    private val events = listOf(
        "🎃", "🎄", "🎆", "🎇", "🧨", "✨", "🎈", "🎉", "🎊", "🎋",
        "🎍", "🎎", "🎏", "🎐", "🎑", "🧧", "🎀", "🎁", "🎗️", "🎟️",
        "🎫"
    )

    private val awards = listOf(
        "🏆", "🏅", "🥇", "🥈", "🥉", "🎖️", "🏵️"
    )

    private val sports = listOf(
        "⚽", "⚾", "🥎", "🏀", "🏐", "🏈", "🏉", "🎾", "🥏", "🎳",
        "🏏", "🏑", "🏒", "🥍", "🏓", "🏸", "🥊", "🥋", "🥅", "⛳",
        "⛸️", "🎣", "🤿", "🎽", "🎿", "🛷", "🥌"
    )

    private val games = listOf(
        "🎯", "🪀", "🪁", "🔫", "🎱", "🔮", "🪄", "🧿", "🪬", "🎮",
        "🕹️", "🎰", "🎲", "🧩", "🧸", "🪅", "🪩", "🪆", "♠️", "♥️",
        "♦️", "♣️", "♟️", "🃏", "🀄", "🎴"
    )

    private val arts = listOf(
        "🎭", "🖼️", "🎨", "🧵", "🪡", "🧶", "🪢", "🎪", "🩰"
    )

    private val music = listOf(
        "🎼", "🎵", "🎶", "🎙️", "🎚️", "🎛️", "🎤", "🎧", "📻", "🎷",
        "🪗", "🎸", "🎹", "🎺", "🎻", "🪕", "🥁", "🪘", "🪇", "🪈",
        "🎬"
    )

    val emojis: List<String> = events + awards + sports + games + arts + music
}
