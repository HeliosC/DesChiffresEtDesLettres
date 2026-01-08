package fr.helios.dcdl.network

import fr.helios.dcdl.dto.GameCreateRequest
import fr.helios.dcdl.dto.GameJoinRequest
import fr.helios.dcdl.dto.GameStartRoundRequest
import fr.helios.dcdl.model.Game
import fr.helios.dcdl.model.GameRoundType
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody

object GameApi {
    //admin
    suspend fun create(gameId: String) =
        ApiClient.client.post("/api/games/create") {
            header("Content-Type", "application/json")
            setBody(GameCreateRequest(gameId = gameId))
        }

    suspend fun start(id: String) {}

    suspend fun startRound(gameId: String, roundType: GameRoundType) =
        ApiClient.client.post("/api/games/startRound") {
            header("Content-Type", "application/json")
            setBody(GameStartRoundRequest(gameId = gameId, roundType = roundType))
        }

    //player
    suspend fun join(gameId: String, username: String) =
        ApiClient.client.post("/api/games/join") {
            header("Content-Type", "application/json")
            setBody(GameJoinRequest(gameId = gameId, username = username))
        }

    suspend fun submit(id: String) {}

    //ws
    suspend fun connectToWebSocket(
        gameId: String,
        onSessionCreated: (DefaultClientWebSocketSession) -> Unit,
        onSessionEnded: (DefaultClientWebSocketSession) -> Unit,
        onUpdateReceived: (Game) -> Unit
    ) {
        ApiClient.client.webSocket("${ApiClient.wsUrl}/ws/games/$gameId") {
            onSessionCreated.invoke(this)

            try {
                while (true) {
                    val game = receiveDeserialized<Game>()
                    onUpdateReceived.invoke(game)
                }
            } catch (e: Exception) {
                println("WebSocket Error: $e")
            } finally {
                onSessionEnded.invoke(this)
            }
        }
    }
}