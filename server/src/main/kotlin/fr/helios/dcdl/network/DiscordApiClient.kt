package fr.helios.dcdl.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object DiscordApiClient {
    const val DISCORD_API_URL = "https://discord.com/api/v10/"

    val client = HttpClient {
        install(HttpTimeout) {
            connectTimeoutMillis = 120_000
            requestTimeoutMillis = 120_000
        }

        install(Logging) {
            level = LogLevel.INFO
        }

        val json = Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }

        install(ContentNegotiation) {
            json(json)
        }

        defaultRequest {
            url(DISCORD_API_URL)
        }
    }
}
