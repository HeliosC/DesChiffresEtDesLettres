package fr.helios.dcdl.network

import io.ktor.client.request.post

object GameApi {
    //admin
    suspend fun create(gameId: String) =
        ApiClient.client.post("/api/games/create") {
            header("Content-Type", "application/json")
            setBody(GameCreateRequest(gameId = gameId))
        }

    suspend fun start(id: String) {
        ApiClient.client.post("games/$id/start")
    }

    //player
    suspend fun join(gameId: String, username: String) =
        ApiClient.client.post("/api/games/join") {
            header("Content-Type", "application/json")
            setBody(GameJoinRequest(gameId = gameId, username = username))
        }

    suspend fun submit(id: String) {
        ApiClient.client.post("games/$id/submit")
    }
}