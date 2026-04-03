package domain.tarot

data class TarotCard(
    val id: Int,
    val name: String,
    val arcana: Arcana,
    val suit: Suit? = null,
    val number: Int,
    val uprightKeywords: List<String>,
    val invertedKeywords: List<String>,
    val isReversed: Boolean = false,
    val position: String = ""
)

enum class Arcana { MAJOR, MINOR }

enum class Suit { CUPS, PENTACLES, SWORDS, WANDS }
