package fr.helios.dcdl.rules

object LettersRules {
    const val ROUND_TIME = 60_000L

    object Tiles {
        const val TO_PICK = 10

        const val MIN_VOWELS = 3
        const val MAX_VOWELS = 6

        const val VOWELS = "AEIOU"
        const val CONSONANTS = "BCDFGHJKLMNPQRSTVWXYZ"
    }

    object Score {
        fun calculateScore(word: String): Int {
            return word.length
        }
    }
}
