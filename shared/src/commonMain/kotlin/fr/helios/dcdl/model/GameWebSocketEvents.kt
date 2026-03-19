package fr.helios.dcdl.model

import kotlinx.serialization.Serializable

@Serializable
data class ClientWsMessage(
    val data: ClientWsMessageData
)

@Serializable
sealed interface ClientWsMessageData {
    @Serializable
    data class SubmitAnswer(
        val answer: ClientRoundAnswer?
    ): ClientWsMessageData

    @Serializable
    data class AcceptAnswer(
        val playerId: String,
        val acceptAnswer: Boolean
    ): ClientWsMessageData
}

@Serializable
sealed interface ClientRoundAnswer {

    @Serializable
    data class Numbers(
        val result: Int,
        val operation: List<NumbersOperation>
    ): ClientRoundAnswer

    @Serializable
    data class Letters(
        val word: String
    ): ClientRoundAnswer
}
