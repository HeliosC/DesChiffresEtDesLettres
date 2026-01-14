package fr.helios.dcdl.rules

object LettersRules {
    const val ROUND_TIME = 30_000L

    object Tiles {
        const val TO_PICK = 10

        const val MIN_VOWELS = 2
        const val MAX_VOWELS = 8

        const val VOWELS = "AEIOU"
        const val CONSONANTS = "BCDFGHJKLMNPQRSTVWXYZ"
    }

    fun getScore(word: String): Int {
        return word.length

    }
}