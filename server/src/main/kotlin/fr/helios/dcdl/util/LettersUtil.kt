package fr.helios.dcdl.util

import fr.helios.dcdl.model.GameRoundData
import fr.helios.dcdl.rules.LettersRules
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
}
