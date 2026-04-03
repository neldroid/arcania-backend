package database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

internal suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }


//fun ResultRow.toTokenResponse(): TokenResponse =
//    TokenResponse(
//        accessToken = this[GoogleTokenTable.accessToken],
//        refreshToken = this[GoogleTokenTable.refreshToken],
//        expiresAt = this[GoogleTokenTable.expireAt]
//    )