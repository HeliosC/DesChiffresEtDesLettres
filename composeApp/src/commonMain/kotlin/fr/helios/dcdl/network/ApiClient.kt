package fr.helios.dcdl.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiClient {
    val client = HttpClient {
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
            //url("http://10.0.2.2:8080/") //for mobile emulator
            url("http://0.0.0.0:8080/")
        }
    }
}