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
import kotlinx.serialization.json.Json

object ApiClient {
    const val PROD_ENV: Boolean = true

    //TODO: dynamic url for dev
    const val SEVER_URL_DOMAIN_PROD = "deschiffresetdeslettres-server.onrender.com"
    const val SEVER_URL_DOMAIN_TEST = "0.0.0.0:8080" // 10.0.2.2:8080 //for mobile emulator

    val httpUrl = if (PROD_ENV) "https://$SEVER_URL_DOMAIN_PROD" else "http://$SEVER_URL_DOMAIN_TEST"
    val wsUrl = if (PROD_ENV) "wss://$SEVER_URL_DOMAIN_PROD" else "ws://$SEVER_URL_DOMAIN_TEST"

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