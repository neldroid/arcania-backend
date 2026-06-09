package domain.repository

import common.model.user.User

interface UserRepository {
    suspend fun findUser(userId: String): User?
    suspend fun findUserIdByEmail(email: String): String?
    suspend fun grantTarotReading(userId: String, readingType: String)
    suspend fun grantDreamInterpretation(userId: String)
    suspend fun grantReikiAppointment(userId: String)
    suspend fun consumeReikiAppointment(userId: String)
}
