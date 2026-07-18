package api.routes.request

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReadingRequestContractTest {

    private val json = Json { ignoreUnknownKeys = false }

    @Test
    fun `deserializes the exact body the frontend sends`() {
        // Mirror of hooks/useReadingSubmit.ts POST body to /api/reading.
        val body = """
            {
              "userId": "u_123",
              "userName": "Nahuel",
              "readingId": "key",
              "question": "¿Qué bloquea mi camino?",
              "isForAnotherPerson": false
            }
        """.trimIndent()

        val request = json.decodeFromString<UserReadingRequest>(body)

        assertEquals("u_123", request.userId)
        assertEquals("Nahuel", request.userName)
        assertEquals("key", request.readingId)
        assertEquals("¿Qué bloquea mi camino?", request.question)
        assertEquals(false, request.isForAnotherPerson)
    }

    @Test
    fun `userName and isForAnotherPerson are optional`() {
        val body = """{ "userId": "u_1", "readingId": "single", "question": "¿Y hoy?" }"""
        val request = json.decodeFromString<UserReadingRequest>(body)

        assertEquals(null, request.userName)
        assertEquals(false, request.isForAnotherPerson)
    }

    @Test
    fun `missing readingId is rejected`() {
        val body = """{ "userId": "u_1", "question": "?" }"""
        assertFailsWith<Exception> { json.decodeFromString<UserReadingRequest>(body) }
    }

    @Test
    fun `question can be omitted, e_g_ for the open signal reading`() {
        val body = """{ "userId": "u_1", "readingId": "signal" }"""
        val request = json.decodeFromString<UserReadingRequest>(body)

        assertEquals("", request.question)
    }

    @Test
    fun `internal ReadTarotRequest carries the same readingId`() {
        val request = ReadTarotRequest(
            userId = "u_1",
            userName = "Nahuel",
            readingType = "shadow",
            question = "?",
        )
        val encoded = json.encodeToString(ReadTarotRequest.serializer(), request)
        val roundTrip = json.decodeFromString<ReadTarotRequest>(encoded)
        assertEquals("shadow", roundTrip.readingType)
    }
}
