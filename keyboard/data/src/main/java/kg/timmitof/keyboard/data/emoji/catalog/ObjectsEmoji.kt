package kg.timmitof.keyboard.data.emoji.catalog

internal object ObjectsEmoji {

    private val clothing = listOf(
        "👓", "🕶️", "🥽", "🥼", "🦺", "👔", "👕", "👖", "🧣", "🧤",
        "🧥", "🧦", "👗", "👘", "🥻", "🩱", "🩲", "🩳", "👙", "👚",
        "🪭", "👛", "👜", "👝", "🛍️", "🎒", "🩴", "👞", "👟", "🥾",
        "🥿", "👠", "👡", "🩰", "👢", "🪮", "👑", "👒", "🎩", "🎓",
        "🧢", "🪖", "⛑️", "📿", "💄", "💍", "💎"
    )

    private val sound = listOf(
        "🔇", "🔈", "🔉", "🔊", "📢", "📣", "📯", "🔔", "🔕"
    )

    private val tech = listOf(
        "📱", "📲", "☎️", "📞", "📟", "📠", "🔋", "🪫", "🔌", "💻",
        "🖥️", "🖨️", "⌨️", "🖱️", "🖲️", "💽", "💾", "💿", "📀", "🧮"
    )

    private val media = listOf(
        "🎥", "🎞️", "📽️", "📺", "📷", "📸", "📹", "📼"
    )

    private val light = listOf(
        "🔍", "🔎", "🕯️", "💡", "🔦", "🏮", "🪔"
    )

    private val books = listOf(
        "📔", "📕", "📖", "📗", "📘", "📙", "📚", "📓", "📒", "📃",
        "📜", "📄", "📰", "🗞️", "📑", "🔖", "🏷️"
    )

    private val money = listOf(
        "💰", "🪙", "💴", "💵", "💶", "💷", "💸", "💳", "🧾", "💹"
    )

    private val mail = listOf(
        "✉️", "📧", "📨", "📩", "📤", "📥", "📦", "📫", "📪", "📬",
        "📭", "📮", "🗳️"
    )

    private val writing = listOf(
        "✏️", "✒️", "🖋️", "🖊️", "🖌️", "🖍️", "📝"
    )

    private val office = listOf(
        "💼", "📁", "📂", "🗂️", "📅", "📆", "🗒️", "🗓️", "📇", "📈",
        "📉", "📊", "📋", "📌", "📍", "📎", "🖇️", "📏", "📐", "✂️",
        "🗃️", "🗄️", "🗑️"
    )

    private val locks = listOf(
        "🔒", "🔓", "🔏", "🔐", "🔑", "🗝️"
    )

    private val tools = listOf(
        "🔨", "🪓", "⛏️", "⚒️", "🛠️", "🗡️", "⚔️", "💣", "🪃", "🏹",
        "🛡️", "🪚", "🔧", "🪛", "🔩", "⚙️", "🗜️", "⚖️", "🔗", "⛓️",
        "🪝", "🧰", "🧲", "🪜"
    )

    private val science = listOf(
        "⚗️", "🧪", "🧫", "🧬", "🔬", "🔭", "📡"
    )

    private val medical = listOf(
        "💉", "🩸", "💊", "🩹", "🩼", "🩺", "🩻"
    )

    private val household = listOf(
        "🚪", "🛗", "🪞", "🪟", "🛏️", "🛋️", "🪑", "🚽", "🪠", "🚿",
        "🛁", "🪤", "🪒", "🧴", "🧷", "🧹", "🧺", "🧻", "🪣", "🧼",
        "🫧", "🪥", "🧽", "🧯", "🛒"
    )

    private val other = listOf(
        "🚬", "⚰️", "🪦", "⚱️", "🪧", "🪪"
    )

    val emojis: List<String> = clothing + sound + tech + media + light + books +
            money + mail + writing + office + locks + tools + science +
            medical + household + other
}
