package fr.helios.dcdl.model

import fr.helios.dcdl.rules.LettersRules
import fr.helios.dcdl.rules.NumbersRules
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val id: String,
    val state: GameState = GameState.WAITING,
    val players: List<Player> = emptyList(),
    val currentRound: GameRound? = null,
    val rounds: List<GameRound> = listOf()
)

@Serializable
enum class GameState {
    WAITING, IN_GAME, FINISHED
}

@Serializable
data class Player(
    val username: String,
    val score: Int = 0
)

@Serializable
sealed interface GameRound {
    val data: GameRoundData
    val startTime: Long
    /** Username - Answer */
    val answers: Map<String, RoundAnswer>

    @Serializable
     data class Numbers(
         override val data: GameRoundData.Numbers,
         override val startTime: Long,
         override val answers: Map<String, RoundAnswer.Numbers> = mapOf()
     ): GameRound

    @Serializable
    data class Letters(
        override val data: GameRoundData.Letters,
        override val startTime: Long,
        override val answers: Map<String, RoundAnswer.Letters> = mapOf()
    ): GameRound
}

@Serializable
sealed class GameRoundData(@SerialName("round_type") val type: GameRoundType) {

    @Serializable
    data class Numbers(
        val objective: Int,
        val tiles: List<Int>
    ): GameRoundData(GameRoundType.NUMBERS)

    @Serializable
    data class Letters(
        val tiles: List<Char>
    ): GameRoundData (GameRoundType.LETTERS)
}

@Serializable
sealed interface RoundAnswer {
    @Serializable
    data class Numbers(
        val result: Int,
        val operation: List<NumbersOperation>
    ): RoundAnswer

    @Serializable
    data class Letters(
        val word: String
    ): RoundAnswer
}

enum class GameRoundType(val duration: Long) {
    NUMBERS(NumbersRules.ROUND_TIME),
    LETTERS(LettersRules.ROUND_TIME)
}

