package fr.helios.dcdl.rules

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
        const val MAX_DIFF_TO_SCORE_PERCENT = 0.21

        fun calculateScore(objective: Int, bestPossibleDiff: Int, playerResult: Int): Int {
            val playerDiff = abs(objective - playerResult)

            if (bestPossibleDiff > playerDiff) {
                println("ERROR in calculateScore: player diff < best possible diff")
                return 0
            }
            val diffFromBest = playerDiff - bestPossibleDiff

            if (diffFromBest == 0) {
                return EXACT
            } else {
                val pointScale = objective * MAX_DIFF_TO_SCORE_PERCENT / NOT_EXACT
                return (NOT_EXACT - floor(diffFromBest / pointScale).toInt()).coerceAtLeast(0)
            }
        }
    }
}
