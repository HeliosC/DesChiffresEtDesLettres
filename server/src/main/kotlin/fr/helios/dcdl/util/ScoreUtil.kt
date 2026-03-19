package fr.helios.dcdl.util

import fr.helios.dcdl.model.GameRound

object ScoreUtil {
    fun getTotalScore(rounds: List<GameRound>): Map<String, Int> =
        rounds.map { round ->
            round.answers.mapValues { answer -> answer.value.score }.entries
        }.flatten().groupBy { allScoresWithId ->
            allScoresWithId.key
        }.mapValues { playerScoresById ->
            playerScoresById.value.sumOf { playerScores -> playerScores.value }
        }
}
