package fr.helios.dcdl.util

import fr.helios.dcdl.model.GameRoundData
import fr.helios.dcdl.rules.NumbersRules
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
}
