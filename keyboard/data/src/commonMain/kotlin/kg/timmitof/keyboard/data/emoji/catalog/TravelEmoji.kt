package kg.timmitof.keyboard.data.emoji.catalog

internal object TravelEmoji {

    private val landscape = listOf(
        "🗺️", "🗾", "🧭", "🏔️", "⛰️", "🌋", "🗻", "🏕️", "🏖️", "🏜️",
        "🏝️", "🏞️", "🪨", "⛱️"
    )

    private val buildings = listOf(
        "🏟️", "🏛️", "🏗️", "🧱", "🛖", "🏘️", "🏚️", "🏠", "🏡", "🏢",
        "🏣", "🏤", "🏥", "🏦", "🏨", "🏩", "🏪", "🏫", "🏬", "🏭",
        "🏯", "🏰", "💒", "🗼", "🗽"
    )

    private val religious = listOf(
        "⛪", "🕌", "🛕", "🕍", "⛩️", "🕋"
    )

    private val city = listOf(
        "⛲", "⛺", "🌁", "🌃", "🏙️", "🌄", "🌅", "🌆", "🌇", "🌉",
        "♨️", "🎠", "🛝", "🎡", "🎢", "💈", "🗿"
    )

    private val ground = listOf(
        "🚂", "🚃", "🚄", "🚅", "🚆", "🚇", "🚈", "🚉", "🚊", "🚝",
        "🚞", "🚋", "🚌", "🚍", "🚎", "🚐", "🚑", "🚒", "🚓", "🚔",
        "🚕", "🚖", "🚗", "🚘", "🚙", "🛻", "🚚", "🚛", "🚜", "🏎️",
        "🏍️", "🛵", "🦽", "🦼", "🛺", "🚲", "🛴", "🛹", "🛼", "🦯"
    )

    private val road = listOf(
        "🚏", "🛣️", "🛤️", "🛢️", "⛽", "🛞", "🚨", "🚥", "🚦", "🛑",
        "🚧"
    )

    private val water = listOf(
        "⚓", "🛟", "⛵", "🛶", "🚤", "🛳️", "⛴️", "🛥️", "🚢"
    )

    private val air = listOf(
        "✈️", "🛩️", "🛫", "🛬", "🪂", "💺", "🚁", "🚟", "🚠", "🚡",
        "🛰️", "🚀", "🛸"
    )

    private val hotel = listOf(
        "🛎️", "🧳"
    )

    private val time = listOf(
        "⌛", "⏳", "⌚", "⏰", "⏱️", "⏲️", "🕰️", "🕛", "🕐", "🕑",
        "🕒", "🕓", "🕔", "🕕", "🕖", "🕗", "🕘", "🕙", "🕚"
    )

    val emojis: List<String> = landscape + buildings + religious + city + ground +
            road + water + air + hotel + time
}
