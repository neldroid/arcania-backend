package common.di

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import data.firebase.TarotReadingRepository
import domain.tarot.TarotService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import java.io.ByteArrayInputStream

private fun loadFirebaseCredentials(): GoogleCredentials? {
    val serviceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT")
    return if (serviceAccountJson != null) {
        GoogleCredentials.fromStream(
            ByteArrayInputStream(serviceAccountJson.toByteArray())
        )
    } else {
        null
    }
}

val appModule = module {
    single<FirebaseApp> {
        if (FirebaseApp.getApps().isNotEmpty()) {
            FirebaseApp.getInstance()
        } else {
            val credentials = loadFirebaseCredentials()
            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build()
            FirebaseApp.initializeApp(options)
        }
    }

    // Inject specific Firebase services you need
    single<Firestore> {
        get<FirebaseApp>() // ensures FirebaseApp is initialized first
        FirestoreClient.getFirestore()
    }

    single {
        HttpClient(CIO){
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 120_000  // 2 minutes
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 120_000
            }
        }
    }

    /* Add repositories injection
    Ex.: single<Repository> {RepositoryImpl()}
    */
    single { TarotReadingRepository(get<Firestore>()) }
    /* Add use cases injection
    Ex.: single { UseCase() }
     */
    single { TarotService(get<TarotReadingRepository>()) }

    /* Add Notifiers
    Ex.: single { Notifier() }
     */
}