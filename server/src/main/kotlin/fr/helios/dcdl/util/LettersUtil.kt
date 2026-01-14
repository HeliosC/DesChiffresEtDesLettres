package fr.helios.dcdl.util

import fr.helios.dcdl.model.GameRoundData
import fr.helios.dcdl.model.Player
import fr.helios.dcdl.model.RoundAnswer
import fr.helios.dcdl.rules.LettersRules
import fr.helios.dcdl.rules.NumbersRules
import kotlin.math.abs
import kotlin.random.Random

object LettersUtil {
    fun generateRound(): GameRoundData.Letters {
        val vowelsNumber = Random.nextInt(LettersRules.Tiles.MIN_VOWELS, LettersRules.Tiles.MAX_VOWELS + 1)

        val tiles = (1..LettersRules.Tiles.TO_PICK).map { index ->
            if (index <= vowelsNumber) LettersRules.Tiles.VOWELS.random()
            else LettersRules.Tiles.CONSONANTS.random()
        }.shuffled()

        return GameRoundData.Letters(tiles)
    }

    fun updateScore(roundData: GameRoundData.Letters, players: List<Player>, answers: Map<String, RoundAnswer.Letters>): List<Player> {
        return players.map { player ->
            val score = answers[player.username]?.word?.let { LettersRules.getScore(it) } ?: 0
            player.copy(
                score = player.score + score
            )
        }
    }
}