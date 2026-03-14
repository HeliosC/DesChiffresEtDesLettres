package fr.helios.dcdl.route

import fr.helios.dcdl.dto.DiscordTokenRequest
import fr.helios.dcdl.dto.DiscordTokenResponse
import fr.helios.dcdl.dto.GameCreateRequest
import fr.helios.dcdl.dto.GameCreateResponse
import fr.helios.dcdl.dto.GameJoinRequest
import fr.helios.dcdl.dto.GameJoinResponse
import fr.helios.dcdl.dto.GameStartRoundRequest
import fr.helios.dcdl.dto.GameStartRoundResponse
import fr.helios.dcdl.service.DiscordService
import fr.helios.dcdl.service.GameService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.util.getOrFail

fun Route.gameRoute(
    gameService: GameService,
) {
    route("/api/games") {
        post("/create") {
            val request = call.receive<GameCreateRequest>()
            val result = gameService.createGame(gameId = request.gameId, adminId = request.adminId)
            call.handleResponse(result) { data ->
                GameCreateResponse(data)
            }
        }

        post("/join") {
            try {
                val request = call.receive<GameJoinRequest>()
                val result = gameService.joinGame(gameId = request.gameId, userId = request.userId, username = request.username)

                call.handleResponse(
                    result,
                ) { data ->
                    GameJoinResponse(game = data)
                }
            } catch (e: Exception) {
                println("GAMEROUTE /JOIN ERROR : $e")
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        post("/joinOrCreate") {
            try {
                val request = call.receive<GameJoinRequest>()
                val result = gameService.getAndCreateGameIfNeeded(gameId = request.gameId, userId = request.userId, username = request.username)

                call.handleResponse(
                    result,
                ) { data ->
                    GameJoinResponse(game = data)
                }
            } catch (e: Exception) {
                println("GAMEROUTE /joinOrCreate ERROR : $e")
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        get("/{id}") {
            val gameId = call.parameters.getOrFail("id")

            val game = gameService.getGame(gameId)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Game with this id doesn't exists")

            call.respond(game)
        }

        post("/startRound") {
            try {
                val request = call.receive<GameStartRoundRequest>()
                val result = gameService.startRound(
                    gameId = request.gameId,
                    roundType = request.roundType
                )

                call.handleResponse(
                    result,
                ) { data ->
                    GameStartRoundResponse(data)
                }
            } catch (e: Exception) {
                println("GAMEROUTE /STARTROUND ERROR : $e")
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}
