package kg.timmitof.keyboard.data.emoji.catalog

/** Порядок подгрупп повторяет Unicode CLDR. */
internal object SmileysEmoji {

    private val happy = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃",
        "🫠", "😉", "😊", "😇", "🥰", "😍", "🤩", "😘", "😗", "☺️",
        "😚", "😙", "🥲"
    )

    private val playful = listOf(
        "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🫢", "🫣",
        "🤫", "🤔", "🫡"
    )

    private val neutral = listOf(
        "🤐", "🤨", "😐", "😑", "😶", "🫥", "😶‍🌫️", "😏", "😒", "🙄",
        "😬", "😮‍💨", "🤥", "🫨", "🙂‍↔️", "🙂‍↕️"
    )

    private val sleepy = listOf(
        "😌", "😔", "😪", "🤤", "😴", "🫩"
    )

    private val unwell = listOf(
        "😷", "🤒", "🤕", "🤢", "🤮", "🤧", "🥵", "🥶", "🥴", "😵",
        "😵‍💫", "🤯"
    )

    private val hats = listOf(
        "🤠", "🥳", "🥸", "😎", "🤓", "🧐"
    )

    private val sad = listOf(
        "😕", "🫤", "😟", "🙁", "☹️", "😮", "😯", "😲", "😳", "🥺",
        "🥹", "😦", "😧", "😨", "😰", "😥", "😢", "😭", "😱", "😖",
        "😣", "😞", "😓", "😩", "😫", "🥱"
    )

    private val angry = listOf(
        "😤", "😡", "😠", "🤬", "😈", "👿", "💀", "☠️"
    )

    private val costume = listOf(
        "💩", "🤡", "👹", "👺", "👻", "👽", "👾", "🤖"
    )

    private val animalFaces = listOf(
        "😺", "😸", "😹", "😻", "😼", "😽", "🙀", "😿", "😾",
        "🙈", "🙉", "🙊"
    )

    private val emotion = listOf(
        "💋", "💯", "💢", "💥", "💫", "💦", "💨", "🕳️", "💬", "👁️‍🗨️",
        "🗨️", "🗯️", "💭", "💤"
    )

    val emojis: List<String> = happy + playful + neutral + sleepy + unwell +
            hats + sad + angry + costume + animalFaces + emotion
}
