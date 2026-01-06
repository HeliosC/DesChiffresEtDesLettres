package fr.helios.dcdl.model

import kotlinx.serialization.Serializable

@Serializable
data class ClientWsMessage(
    val data: ClientWsMessageData
)

@Serializable
sealed interface ClientWsMessageData {
    @Serializable
    data class SubmitResponse(
        val username: String,
        val answer: RoundAnswer?
    ): ClientWsMessageData
}