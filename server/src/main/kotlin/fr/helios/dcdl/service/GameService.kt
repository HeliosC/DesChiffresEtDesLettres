package fr.helios.dcdl.service

import fr.helios.dcdl.model.ClientRoundAnswer
import fr.helios.dcdl.model.Game
import fr.helios.dcdl.model.GameRound
import fr.helios.dcdl.model.GameRoundType
import fr.helios.dcdl.model.GameState
import fr.helios.dcdl.model.Player
import fr.helios.dcdl.model.RoundAnswer
import fr.helios.dcdl.repository.GameRepository
import fr.helios.dcdl.rules.LettersRules
import fr.helios.dcdl.rules.NumbersRules
import fr.helios.dcdl.util.LettersUtil
import fr.helios.dcdl.util.NumberUtil
import fr.helios.dcdl.util.ScoreUtil
import fr.helios.dcdl.websocket.GameUpdateBroadcaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface IGameService {
    fun createGame(gameId: String, adminId: String): Result<Game>
    suspend fun startRound(gameId: String, roundType: GameRoundType): Result<Game>
    //fun startGame()

    suspend fun joinGame(gameId: String, userId: String, username: String): Result<Game>
    suspend fun getAndCreateGameIfNeeded(gameId: String, userId: String, username: String): Result<Game>
    fun getGame(gameId: String): Game?
    fun submitAnswer(gameId: String, userId: String, answer: ClientRoundAnswer?): Result<Game>
    suspend fun acceptAnswer(gameId: String, userId: String, accept: Boolean): Result<Game>
    suspend fun setAdmin(gameId: String, newAdminId: String): Result<Game>
    fun deleteGame(gameId: String): Result<Game?>
}

class GameService(
    private val gameRepository: GameRepository,
    private val gameUpdateBroadcaster: GameUpdateBroadcaster
): IGameService {
    companion object {
        const val SUBMIT_ANSWER_DELAY = 2000L
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val activeTimers = mutableMapOf<String, Job>()

    override fun createGame(gameId: String, adminId: String): Result<Game> {
        if (gameId.isBlank()) {
            return Result.failure(IllegalArgumentException("GameId is empty"))
        }

        if (gameRepository.findGameById(gameId) != null) {
            return Result.failure(IllegalStateException("This game already exists"))
        }

        val game = gameRepository.createGame(gameId = gameId, adminId = adminId)
        return Result.success(game)
    }

    override suspend fun joinGame(gameId: String, userId: String, username: String): Result<Game> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("userId is empty"))
        }

        if (gameRepository.findAllGame().any {
            it.players.any { player -> player.userId == userId }
        }) {
            return Result.failure(IllegalStateException("This user is already in a game"))
        }

        val game = gameRepository.findGameById(gameId)
            ?: return Result.failure(IllegalStateException("This game doesn't exists"))

        if (game.state != GameState.WAITING) {
            return Result.failure(IllegalStateException("This game has already started"))
        }

        val gameWithPlayer = game.copy(players = game.players + Player(userId = userId, username = username))
        gameRepository.saveGame(gameWithPlayer)
        gameUpdateBroadcaster.broadcastGameUpdate(gameId)

        return Result.success(gameWithPlayer)
    }

    override suspend fun getAndCreateGameIfNeeded(gameId: String, userId: String, username: String): Result<Game> {
        if (gameId.isBlank()) {
            return Result.failure(IllegalArgumentException("GameId is empty"))
        }

        val game = getGame(gameId)
        if (game == null) {
            val newGame = gameRepository.createGame(gameId = gameId, adminId = userId)
            return joinGame(gameId = newGame.id, userId = userId, username = username)
        } else {
            return if (game.players.any { it.userId == userId }) {
                Result.success(game)
            } else {
                joinGame(gameId = gameId, userId = userId, username = username)
            }
        }
    }

    override fun getGame(gameId: String): Game? {
        return gameRepository.findGameById(gameId)
    }

    override suspend fun startRound(gameId: String, roundType: GameRoundType): Result<Game> {
        val game = gameRepository.findGameById(gameId)
            ?: return Result.failure(IllegalStateException("This game doesn't exists"))

        val newRound = when (roundType) {
            GameRoundType.NUMBERS ->
                GameRound.Numbers(
                    startTime = System.currentTimeMillis(),
                    data = NumberUtil.generateRound()
                )
            GameRoundType.LETTERS ->
                GameRound.Letters(
                    startTime = System.currentTimeMillis(),
                    data = LettersUtil.generateRound()
                )
        }

        val gameWithRound = game.copy(
            currentRound = newRound
        )
        gameRepository.saveGame(gameWithRound)
        gameUpdateBroadcaster.broadcastGameUpdate(gameId)

        activeTimers[gameId]?.cancel()
        activeTimers[gameId] = serviceScope.launch {
            delay(roundType.duration + SUBMIT_ANSWER_DELAY)
            endRound(gameId)
            activeTimers[gameId]?.cancel()
        }

        return Result.success(gameWithRound)
    }

    private suspend fun endRound(gameId: String) {
        val game = gameRepository.findGameById(gameId) ?: return
        val currentRound = game.currentRound ?: return

        val totalScore: Map<String, Int> = ScoreUtil.getTotalScore(game.rounds + currentRound)

        val updatedPlayers = game.players.map { player ->
            player.copy(
                score = totalScore[player.userId] ?: 0
            )
        }

        val updatedGame = game.copy(
            currentRound = null,
            players = updatedPlayers,
            rounds = game.rounds + currentRound
        )
        gameRepository.saveGame(updatedGame)
        gameUpdateBroadcaster.broadcastGameUpdate(gameId)
    }

    override fun submitAnswer(gameId: String, userId: String, answer: ClientRoundAnswer?): Result<Game> {
        val game = gameRepository.findGameById(gameId) ?: return Result.failure(IllegalStateException("Game not found"))
        val currentRound = game.currentRound ?: return Result.failure(IllegalStateException("No round ongoing"))

        if (System.currentTimeMillis() - currentRound.startTime > currentRound.data.type.duration + SUBMIT_ANSWER_DELAY) {
            return Result.failure(IllegalStateException("Round timed out"))
        }

        val updatedRound = when (currentRound) {
            is GameRound.Numbers -> {
                if (answer !is ClientRoundAnswer.Numbers) return Result.failure(IllegalStateException("Wrong answer type"))

                //TODO: calculate bestPossibleDiff
                val serverAnswer = RoundAnswer.Numbers(
                    result = answer.result,
                    operation = answer.operation,
                    score = NumbersRules.Score.calculateScore(
                        objective = currentRound.data.objective,
                        bestPossibleDiff = 0,
                        playerResult = answer.result
                    )
                )

                val updatedAnswers = currentRound.answers + (userId to serverAnswer)
                currentRound.copy(answers = updatedAnswers)
            }
            is GameRound.Letters -> {
                if (answer !is ClientRoundAnswer.Letters) return Result.failure(IllegalStateException("Wrong answer type"))

                val serverAnswer = RoundAnswer.Letters(
                    word = answer.word,
                    score = LettersRules.Score.calculateScore(
                        word = answer.word
                    )
                )

                val updatedAnswers = currentRound.answers + (userId to serverAnswer)
                currentRound.copy(answers = updatedAnswers)
            }
        }

        val updatedGame = game.copy(currentRound = updatedRound)
        gameRepository.saveGame(updatedGame)

        return Result.success(updatedGame)
    }

    override suspend fun acceptAnswer(gameId: String, userId: String, accept: Boolean): Result<Game> {
        val game = gameRepository.findGameById(gameId)
            ?: return Result.failure(IllegalStateException("This game doesn't exists"))

        if (game.currentRound != null) {
            return Result.failure(IllegalStateException("We are currently in a round"))
        }

        val lastRound = game.rounds.lastOrNull() ?:
            return Result.failure(IllegalStateException("No rounds in history"))

        val newRound: GameRound = when(lastRound) {
            is GameRound.Numbers -> {
                lastRound.copy(answers =
                    lastRound.answers.toMutableMap().also { answers ->
                        val answer = answers[userId] ?:
                            return Result.failure(IllegalStateException("No answers for this player this round"))

                        answers[userId] = answer.copy(
                            score = if (accept) {
                                NumbersRules.Score.calculateScore(
                                    objective = lastRound.data.objective,
                                    bestPossibleDiff = 0,
                                    playerResult = answer.result
                                )
                            } else {
                                0
                            }
                        )
                    }
                )
            }

            is GameRound.Letters -> {
                lastRound.copy(answers =
                    lastRound.answers.toMutableMap().also { answers ->
                        val answer = answers[userId] ?:
                            return Result.failure(IllegalStateException("No answers for this player this round"))

                        answers[userId] = answer.copy(
                            score = if (accept) {
                                LettersRules.Score.calculateScore(
                                    word = answer.word
                                )
                            } else {
                                0
                            }
                        )
                    }
                )
            }
        }

        val newRounds = game.rounds.dropLast(1) + newRound

        val totalScore: Map<String, Int> = ScoreUtil.getTotalScore(newRounds)
        val updatedPlayers = game.players.map { player ->
            player.copy(
                score = totalScore[player.userId] ?: 0
            )
        }

        val updatedGame = game.copy(
            players = updatedPlayers,
            rounds = newRounds
        )
        gameRepository.saveGame(updatedGame)
        gameUpdateBroadcaster.broadcastGameUpdate(gameId)

        return Result.success(updatedGame)
    }

    override suspend fun setAdmin(gameId: String, newAdminId: String): Result<Game> {
        if (newAdminId.isBlank()) {
            return Result.failure(IllegalArgumentException("newAdminId is empty"))
        }

        val game = gameRepository.findGameById(gameId)
            ?: return Result.failure(IllegalStateException("This game doesn't exists"))

        val updatedGame = game.copy(adminId = newAdminId)
        gameRepository.saveGame(updatedGame)
        gameUpdateBroadcaster.broadcastGameUpdate(gameId)

        return Result.success(updatedGame)
    }

    override fun deleteGame(gameId: String): Result<Game?> {
        if (gameId.isBlank()) {
            return Result.failure(IllegalArgumentException("GameId is empty"))
        }

        return Result.success(gameRepository.deleteGame(gameId))
    }
}
