package fr.helios.dcdl.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.atomicfu.locks.synchronized
import kotlinx.serialization.json.Json
import kotlin.concurrent.Volatile

class ApiClient private constructor(private val host: String) {

    companion object {
        @Volatile
        private var instance: ApiClient? = null

        fun setInstance(host: String) =
            instance ?: synchronized(this) {
                instance ?: ApiClient(host).also { instance = it }
            }

        fun getInstance(): ApiClient? = instance

        val client: HttpClient
            get() = getInstance()?.client!! //TODO: dependency injection

        const val PROD_ENV: Boolean = true

        //TODO: dynamic url for dev
        const val SEVER_URL_DOMAIN_PROD = "deschiffresetdeslettres-server.onrender.com"
        const val SEVER_URL_DOMAIN_TEST = "127.0.0.1:8080" // 10.0.2.2:8080 //for mobile emulator

        val httpUrl
            get() = (if (PROD_ENV) "https" else "http") + "://${instance?.host}" //if (PROD_ENV) "https://$SEVER_URL_DOMAIN_PROD" else "http://$SEVER_URL_DOMAIN_TEST"

        val wsUrl
            get() = (if (PROD_ENV) "wss" else "ws") + "://${instance?.host}" // if (PROD_ENV) "wss://$SEVER_URL_DOMAIN_PROD" else "ws://$SEVER_URL_DOMAIN_TEST"
    }



    val client = HttpClient {
        install(HttpTimeout) {
            connectTimeoutMillis = 120_000
            requestTimeoutMillis = 120_000
        }

        install(Logging) {
            level = LogLevel.INFO // or ALL
        }

        val json = Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }

        install(ContentNegotiation) {
            json(json)
        }

        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(json)
            pingIntervalMillis = 20_000
        }

        defaultRequest {
            url(httpUrl)
        }
    }
}
