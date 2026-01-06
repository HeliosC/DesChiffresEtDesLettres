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
        route.webSocket("/ws/games/{id}") {
            val gameId = call.parameters.getOrFail("id")

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
                        is ClientWsMessageData.SubmitResponse -> {
                            gameService.submitResponse(
                                gameId = gameId,
                                username = data.username,
                                answer = data.answer
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                println("WebSocket server Error: $e")
            } finally {
                val gameSessions = sessions[gameId]?.toMutableList() ?: mutableListOf()
                gameSessions.remove(this)
                sessions[gameId] = gameSessions
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