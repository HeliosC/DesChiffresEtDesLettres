package fr.helios.dcdl.websocket

import fr.helios.dcdl.model.ClientWsMessage
import fr.helios.dcdl.model.ClientWsMessageData
import fr.helios.dcdl.service.GameService
import io.ktor.server.routing.Route
import io.ktor.server.util.getOrFail
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.util.Collections

interface GameUpdateBroadcaster {
    suspend fun broadcastGameUpdate(gameId: String)
}

class GameWebSocketHandler(private var gameService: GameService): GameUpdateBroadcaster {

    fun setGameService(gameService: GameService) {
        this.gameService = gameService
    }

    private val sessions: MutableMap<String?, MutableList<WebSocketServerSession>?> = Collections.synchronizedMap<String, MutableList<WebSocketServerSession>>(mutableMapOf())

    fun webSocketRoutes(route: Route) {
        route.webSocket("/ws/game/{gameId}/player/{playerId}") {
            val gameId = call.parameters.getOrFail("gameId")
            val playerId = call.parameters.getOrFail("playerId")

            val game = gameService.getGame(gameId)
                ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No game ID"))

            val gameSessions = sessions[gameId]?.toMutableList() ?: mutableListOf()
            gameSessions.add(this)
            sessions[gameId] = gameSessions
            sendSerialized(game)

            try {
                while (true) {
                    val receivedData = receiveDeserialized<ClientWsMessage>()

                    when (val data = receivedData.data) {
                        is ClientWsMessageData.SubmitAnswer -> {
                            gameService.submitResponse(
                                gameId = gameId,
                                userId = playerId,
                                answer = data.answer
                            )
                        }
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                println("WebSocket Closed: $e")
            } catch (e: Exception) {
                println("WebSocket server Error: $e")
                e.printStackTrace()
            } finally {
                println("WebSocket Closed reason: ${closeReason.await()}")
                val gameSessions = sessions[gameId]?.toMutableList() ?: mutableListOf()
                gameSessions.remove(this)
                sessions[gameId] = gameSessions

                //delete game if nobody's left
                if (gameSessions.isEmpty()) {
                    gameService.deleteGame(gameId)
                } else {
                    gameService.getGame(gameId)?.let { game ->
                        //change admin if admin left
                        if (playerId == game.adminId) {
                            val newAdminId = game.players.firstOrNull { it.userId != playerId }?.userId
                            if (newAdminId != null) {
                                gameService.setAdmin(gameId, newAdminId)
                                broadcastGameUpdate(gameId)
                            } else {
                                gameService.deleteGame(gameId)
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun broadcastGameUpdate(gameId: String) {
        val game = gameService.getGame(gameId) ?: return

        sessions[gameId]?.forEach { session ->
            session.sendSerialized(game)
        }
    }
}
