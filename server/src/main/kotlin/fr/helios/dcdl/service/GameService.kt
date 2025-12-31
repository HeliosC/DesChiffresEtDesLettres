package fr.helios.dcdl.service

import fr.helios.dcdl.model.Game
import fr.helios.dcdl.model.GameState
import fr.helios.dcdl.model.Player
import fr.helios.dcdl.repository.GameRepository

interface IGameService {
    fun createGame(gameId: String): Result<Game>
    //fun startGame()
    //fun deleteGame()

    fun joinGame(gameId: String, username: String): Result<Game>
    fun getGame(gameId: String): Game?
}

class GameService(private val gameRepository: GameRepository): IGameService {
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

    override fun joinGame(gameId: String, username: String): Result<Game> {
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

        return Result.success(gameWithPlayer)
    }

    override fun getGame(gameId: String): Game? {
        return gameRepository.findGameById(gameId)
    }
}