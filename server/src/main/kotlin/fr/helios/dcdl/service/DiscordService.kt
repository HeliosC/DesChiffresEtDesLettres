package fr.helios.dcdl.service

import fr.helios.dcdl.dto.DiscordOauthResponse
import fr.helios.dcdl.network.DiscordApi
import io.ktor.client.call.body

interface IDiscordService {
    suspend fun getToken(code: String): Result<String>
}

class DiscordService(): IDiscordService {
    override suspend fun getToken(code: String): Result<String> {
        val response = DiscordApi.getToken(
            clientId = System.getenv("DISCORD_CLIENT_ID"),
            clientSecret = System.getenv("DISCORD_CLIENT_SECRET"),
            code = code
        )

        return try {
            Result.success(response.body<DiscordOauthResponse>().accessToken)
        } catch (e: Exception) {
            println("ERROR - DISCORD SERVICE GET TOKEN: $e")
            Result.failure(e)
        }
    }
}
