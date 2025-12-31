package fr.helios.dcdl.network

import io.ktor.client.request.post

object GameApi {
    //admin
    suspend fun create(id: String) {
        ApiClient.client.post("games/$id/create")
    }

    suspend fun start(id: String) {
        ApiClient.client.post("games/$id/start")
    }

    //player
    suspend fun join(id: String) {
        ApiClient.client.post("games/$id/join")
    }

    suspend fun submit(id: String) {
        ApiClient.client.post("games/$id/submit")
    }
}