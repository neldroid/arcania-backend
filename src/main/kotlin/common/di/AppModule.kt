package common.di

import database.repository.tarot.TarotReadingsRepository
import database.repository.tarot.TarotReadingsRepositoryImpl
import domain.tarot.TarotService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

val appModule = module {
    single {
        HttpClient(CIO){
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
    single {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/ktor_tutorial_db",
            user = "postgres",
            password = "password"
        )
    }

    /* Add repositories injection
    Ex.: single<Repository> {RepositoryImpl()}
    */
    single<TarotReadingsRepository> { TarotReadingsRepositoryImpl() }

    /* Add use cases injection
    Ex.: single { UseCase() }
     */
    single { TarotService(get()) }

    /* Add Notifiers
    Ex.: single { Notifier() }
     */
}