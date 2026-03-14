package fr.helios.dcdl.route

import fr.helios.dcdl.dto.DiscordTokenRequest
import fr.helios.dcdl.dto.DiscordTokenResponse
import fr.helios.dcdl.service.DiscordService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.discordRoute(
    discordService: DiscordService
) {
    route("/api/discord") {
        post("/token") {
            try {
                val request = call.receive<DiscordTokenRequest>()
                val result = discordService.getToken(
                    code = request.code
                )
                
                call.handleResponse(
                    result,
                ) { data ->
                    DiscordTokenResponse(data)
                }
            } catch (e: Exception) {
                println("DISCORD ROUTE /token ERROR : $e")
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}
