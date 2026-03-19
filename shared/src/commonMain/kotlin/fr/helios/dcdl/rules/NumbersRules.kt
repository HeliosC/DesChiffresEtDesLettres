package fr.helios.dcdl.rules

import fr.helios.dcdl.model.GameRoundData
import fr.helios.dcdl.model.Player
import fr.helios.dcdl.model.RoundAnswer
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.abs
import kotlin.math.floor

object NumbersRules {
    const val ROUND_TIME = 60_000L

    object Objective {
        const val MIN = 101
        const val MAX = 999
    }

    object Tiles {
        const val TO_PICK = 6

        val tiles: List<Int> = mutableListOf<Int>().apply {
            addAll(1..9)
            addAll(1..9)
            addAll(listOf(25, 50, 75, 100))
        }
    }

    object Result {
        const val MIN = 0
        const val MAX = 9999
    }

    object Score {
        const val EXACT = 10
        const val NOT_EXACT = 7

        fun calculateScore(roundData: GameRoundData.Numbers, players: List<Player>, answers: Map<String, RoundAnswer.Numbers>): List<Player> {
            val diffs = answers.mapValues { (_, answer) -> abs(roundData.objective - answer.result) }
            val bestDiff = diffs.minOfOrNull { it.value } ?: 0

            val score = if (bestDiff == 0) {
                NumbersRules.Score.EXACT
            } else {
                NumbersRules.Score.NOT_EXACT
            }

            val playerWhoScored =  diffs.filter { it.value == bestDiff }.keys
            return players.map { player ->
                player.copy(
                    score = if (player.userId in playerWhoScored) {
                        player.score + score
                    } else {
                        player.score
                    }
                )
            }
        }
    }
}
