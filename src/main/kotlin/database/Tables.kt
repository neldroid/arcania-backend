package database

import org.jetbrains.exposed.sql.Table

//object GoogleTokenTable : Table() {
//    val tenantId = varchar("tenantId", 50).uniqueIndex()
//    val accessToken = text("accessToken")
//    val refreshToken = text("refreshToken").nullable()
//    val expireAt = long("expireAt").default(0)
//
//    override val primaryKey = PrimaryKey(tenantId)
//}