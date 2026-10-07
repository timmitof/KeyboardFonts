package kg.timmitof.keyboard.data.emoji.catalog

internal object FoodEmoji {

    private val fruits = listOf(
        "🍇", "🍈", "🍉", "🍊", "🍋", "🍋‍🟩", "🍌", "🍍", "🥭", "🍎",
        "🍏", "🍐", "🍑", "🍒", "🍓", "🫐", "🥝", "🍅", "🫒", "🥥"
    )

    private val vegetables = listOf(
        "🥑", "🍆", "🥔", "🥕", "🌽", "🌶️", "🫑", "🥒", "🥬", "🥦",
        "🧄", "🧅", "🥜", "🫘", "🌰", "🫚", "🫛", "🍄‍🟫"
    )

    private val bakery = listOf(
        "🍞", "🥐", "🥖", "🫓", "🥨", "🥯", "🥞", "🧇", "🧀", "🥚",
        "🍳", "🧈", "🥓"
    )

    private val meals = listOf(
        "🍖", "🍗", "🥩", "🍔", "🍟", "🍕", "🌭", "🥪", "🌮", "🌯",
        "🫔", "🥙", "🧆", "🥘", "🍲", "🫕", "🥣", "🥗", "🍿", "🧂",
        "🥫"
    )

    private val asian = listOf(
        "🍱", "🍘", "🍙", "🍚", "🍛", "🍜", "🍝", "🍠", "🍢", "🍣",
        "🍤", "🍥", "🥮", "🍡", "🥟", "🥠", "🥡", "🦪"
    )

    private val sweets = listOf(
        "🍦", "🍧", "🍨", "🍩", "🍪", "🎂", "🍰", "🧁", "🥧", "🍫",
        "🍬", "🍭", "🍮", "🍯"
    )

    private val drinks = listOf(
        "🍼", "🥛", "☕", "🫖", "🍵", "🍶", "🍾", "🍷", "🍸", "🍹",
        "🍺", "🍻", "🥂", "🥃", "🫗", "🥤", "🧋", "🧃", "🧉", "🧊"
    )

    private val tableware = listOf(
        "🥢", "🍽️", "🍴", "🥄", "🔪", "🫙", "🏺"
    )

    val emojis: List<String> = fruits + vegetables + bakery + meals + asian +
            sweets + drinks + tableware
}
