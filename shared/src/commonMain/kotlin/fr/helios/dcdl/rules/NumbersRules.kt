package fr.helios.dcdl.rules

object NumbersRules {
    const val ROUND_TIME = 40_000L

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
    }
}