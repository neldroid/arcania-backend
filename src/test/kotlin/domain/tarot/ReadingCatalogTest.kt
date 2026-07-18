package domain.tarot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReadingCatalogTest {

    @Test
    fun `known reading ids resolve`() {
        for (id in listOf("single", "three", "key", "shadow", "celtic-cross", "duality", "signal")) {
            val def = ReadingCatalog.get(id)
            assertTrue(def != null, "expected a definition for '$id'")
        }
    }

    @Test
    fun `question is required by default but not for signal`() {
        assertTrue(ReadingCatalog.get("single")!!.questionRequired)
        assertTrue(ReadingCatalog.get("key")!!.questionRequired)
        assertTrue(!ReadingCatalog.get("signal")!!.questionRequired)
    }

    @Test
    fun `signal is a 3-card open reading`() {
        val signal = ReadingCatalog.get("signal")!!
        assertEquals(3, signal.cardsQuantity)
        assertEquals(listOf("Energía presente", "Influencia activa", "Dirección próxima"), signal.positions)
    }

    @Test
    fun `unknown reading id returns null`() {
        assertNull(ReadingCatalog.get("does-not-exist"))
        assertNull(ReadingCatalog.get(""))
    }

    @Test
    fun `cardsQuantity mirrors the number of positions`() {
        val single = ReadingCatalog.get("single")!!
        val three = ReadingCatalog.get("three")!!
        val key = ReadingCatalog.get("key")!!

        assertEquals(1, single.cardsQuantity)
        assertEquals(3, three.cardsQuantity)
        assertEquals(8, key.cardsQuantity)
        assertEquals(key.positions.size, key.cardsQuantity)
    }

    @Test
    fun `every definition is well formed`() {
        // Reach the private catalog through the ids we know the frontend ships.
        val ids = listOf(
            "single", "three", "key", "mirror", "celtic-cross", "relationship",
            "tree-of-life", "yes-no", "horseshoe", "soul-reading", "the-path",
            "shadow", "the-star", "three-wishes", "labyrinth", "karma", "portal",
            "money", "universe-message", "pyramid", "self-love", "judgment",
            "the-wheel", "conflict", "inner-oracle", "duality", "signal",
        )
        for (id in ids) {
            val def = ReadingCatalog.get(id)!!
            assertEquals(id, def.id)
            assertTrue(def.positions.isNotEmpty(), "$id has no positions")
            assertTrue(def.positions.none { it.isBlank() }, "$id has a blank position")
            assertTrue(def.displayName.isNotBlank(), "$id has a blank displayName")
            assertTrue(def.promptGuidance.isNotBlank(), "$id has blank guidance")
        }
    }
}
