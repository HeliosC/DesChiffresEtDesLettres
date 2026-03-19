package fr.helios.dcdl

import fr.helios.dcdl.model.GameRound
import fr.helios.dcdl.model.GameRoundData
import fr.helios.dcdl.model.RoundAnswer
import fr.helios.dcdl.util.ScoreUtil
import org.junit.Test
import kotlin.test.assertEquals

class ScoresTest {
    @Test
    fun getTotalScoresTest() {
        val answer = RoundAnswer.Letters(
            "",
            0
        )

        val round = GameRound.Letters(GameRoundData.Letters(listOf()), 0)

        val round1 = round.copy(
            answers = mapOf(
                "0" to answer.copy(score = 0),
                "1" to answer.copy(score = 1),
                "5" to answer.copy(score = 5),
            )
        )

        val round2 = round.copy(
            answers = mapOf(
                //"0" to answer.copy(score = 10),
                "1" to answer.copy(score = 11),
                "5" to answer.copy(score = 15),
            )
        )

        val round3 = round.copy(
            answers = mapOf(
                "0" to answer.copy(score = 20),
                "1" to answer.copy(score = 21),
                "5" to answer.copy(score = 25),
            )
        )

        val rounds = listOf(round1, round2, round3)

        val totalScore: Map<String, Int> = ScoreUtil.getTotalScore(rounds)

        assertEquals(
            totalScore,
            mapOf("0" to 20, "1" to 33, "5" to 45)
        )
    }
}
