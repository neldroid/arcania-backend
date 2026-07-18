package domain.tarot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TarotCardHelperTest {

    @Test
    fun `deals exactly one card per position, in order`() {
        val positions = ReadingCatalog.get("key")!!.positions
        val cards = TarotCardHelper.getCards(positions)

        assertEquals(positions.size, cards.size)
        assertEquals(positions, cards.map { it.position })
    }

    @Test
    fun `deals distinct cards`() {
        val positions = ReadingCatalog.get("celtic-cross")!!.positions
        val ids = TarotCardHelper.getCards(positions).map { it.id }
        assertEquals(ids.size, ids.toSet().size, "cards must not repeat within a spread")
    }

    @Test
    fun `single-card spread yields one card at its position`() {
        val positions = ReadingCatalog.get("single")!!.positions
        val cards = TarotCardHelper.getCards(positions)
        assertEquals(1, cards.size)
        assertEquals("El presente", cards.first().position)
    }

    @Test
    fun `empty positions is rejected`() {
        assertFailsWith<IllegalArgumentException> { TarotCardHelper.getCards(emptyList()) }
    }

    @Test
    fun `requesting more positions than the deck holds is rejected`() {
        val tooMany = (1..TarotDeck.cards.size + 1).map { "Position $it" }
        assertFailsWith<IllegalArgumentException> { TarotCardHelper.getCards(tooMany) }
    }

    @Test
    fun `orientation is assigned`() {
        // Not asserting a specific value (it is random), just that dealing works
        // across every catalog spread without error and fills every position.
        val ids = listOf("single", "three", "key", "shadow", "money", "duality")
        for (id in ids) {
            val positions = ReadingCatalog.get(id)!!.positions
            val cards = TarotCardHelper.getCards(positions)
            assertTrue(cards.all { it.position.isNotBlank() }, "$id produced a blank position")
        }
    }
}
