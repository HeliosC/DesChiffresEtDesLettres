package fr.helios.dcdl.util

import fr.helios.dcdl.model.GameRoundData
import fr.helios.dcdl.model.Player
import fr.helios.dcdl.model.RoundAnswer
import fr.helios.dcdl.rules.NumbersRules
import kotlin.math.abs
import kotlin.random.Random

object NumberUtil {
    fun generateRound(): GameRoundData.Numbers {
        val objective = Random.nextInt(NumbersRules.Objective.MIN, NumbersRules.Objective.MAX + 1)
        val tiles = NumbersRules.Tiles.tiles.shuffled().take(NumbersRules.Tiles.TO_PICK)

        return GameRoundData.Numbers(
            objective = objective,
            tiles = tiles
        )
    }

    fun updateScore(roundData: GameRoundData.Numbers, players: List<Player>, answers: Map<String, RoundAnswer.Numbers>): List<Player> {
        val diffs = answers.mapValues { (_, answer) -> abs(roundData.objective - answer.result) }
        val bestDiff = diffs.minOf { it.value }

        val score = if (bestDiff == 0) {
            NumbersRules.Score.EXACT
        } else {
            NumbersRules.Score.NOT_EXACT
        }

        val playerWhoScored =  diffs.filter { it.value == bestDiff }.keys
        return players.map { player ->
            player.copy(
                score = if (player.username in playerWhoScored) {
                    player.score + score
                } else {
                    player.score
                }
            )
        }
    }
}