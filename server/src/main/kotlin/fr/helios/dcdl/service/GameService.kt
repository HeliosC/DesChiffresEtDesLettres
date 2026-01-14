package fr.helios.dcdl.service

import fr.helios.dcdl.model.Game
import fr.helios.dcdl.model.GameRound
import fr.helios.dcdl.model.GameRoundType
import fr.helios.dcdl.model.GameState
import fr.helios.dcdl.model.Player
import fr.helios.dcdl.model.RoundAnswer
import fr.helios.dcdl.repository.GameRepository
import fr.helios.dcdl.util.LettersUtil
import fr.helios.dcdl.util.NumberUtil
import fr.helios.dcdl.websocket.GameUpdateBroadcaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface IGameService {
    fun createGame(gameId: String): Result<Game>
    suspend fun startRound(gameId: String, roundType: GameRoundType): Result<Game>
    //fun startGame()
    //fun deleteGame()

    suspend fun joinGame(gameId: String, username: String): Result<Game>
    fun getGame(gameId: String): Game?
    fun submitResponse(gameId: String, username: String, answer: RoundAnswer?): Result<Game>
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

    override fun createGame(gameId: String): Result<Game> {
        if (gameId.isBlank()) {
            return Result.failure(IllegalArgumentException("GameId is empty"))
        }

        if (gameRepository.findGameById(gameId) != null) {
            return Result.failure(IllegalStateException("This game already exists"))
        }

        val game = gameRepository.createGame(gameId)
        return Result.success(game)
    }

    override suspend fun joinGame(gameId: String, username: String): Result<Game> {
        if (username.isBlank()) {
            return Result.failure(IllegalArgumentException("Username is empty"))
        }

        if (gameRepository.findAllGame().any { it.players.any { player -> player.username == username } }) {
            return Result.failure(IllegalStateException("This username is already in a game"))
        }

        val game = gameRepository.findGameById(gameId)
            ?: return Result.failure(IllegalStateException("This game doesn't exists"))

        if (game.state != GameState.WAITING) {
            return Result.failure(IllegalStateException("This game has already started"))
        }

        val gameWithPlayer = game.copy(players = game.players + Player(username))
        gameRepository.saveGame(gameWithPlayer)
        gameUpdateBroadcaster.broadcastGameUpdate(gameId)

        return Result.success(gameWithPlayer)
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

        val updatedPlayers = when (currentRound) {
            is GameRound.Numbers -> NumberUtil.updateScore(
                currentRound.data,
                game.players,
                currentRound.answers
            )
            is GameRound.Letters -> LettersUtil.updateScore(
                currentRound.data,
                game.players,
                currentRound.answers
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

    override fun submitResponse(gameId: String, username: String, answer: RoundAnswer?): Result<Game> {
        val game = gameRepository.findGameById(gameId) ?: return Result.failure(IllegalStateException("Game not found"))
        val currentRound = game.currentRound ?: return Result.failure(IllegalStateException("No round ongoing"))

        if (System.currentTimeMillis() - currentRound.startTime > currentRound.data.type.duration + SUBMIT_ANSWER_DELAY) {
            return Result.failure(IllegalStateException("Round timed out"))
        }

        val updatedRound = when (currentRound) {
            is GameRound.Numbers -> {
                if (answer !is RoundAnswer.Numbers) return Result.failure(IllegalStateException("Wrong answer type"))

                val updatedAnswers = currentRound.answers + (username to answer)
                currentRound.copy(answers = updatedAnswers)
            }
            is GameRound.Letters -> {
                if (answer !is RoundAnswer.Letters) return Result.failure(IllegalStateException("Wrong answer type"))

                val updatedAnswers = currentRound.answers + (username to answer)
                currentRound.copy(answers = updatedAnswers)
            }
        }

        val updatedGame = game.copy(currentRound = updatedRound)
        gameRepository.saveGame(updatedGame)

        return Result.success(updatedGame)
    }
}