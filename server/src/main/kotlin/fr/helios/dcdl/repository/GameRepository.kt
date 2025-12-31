package fr.helios.dcdl.repository

import fr.helios.dcdl.model.Game

interface IGameRepository {
    fun createGame(gameId: String): Game
    fun saveGame(game: Game)
    fun deleteGame(gameId: String): Game?

    fun findGameById(gameId: String): Game?
    fun findAllGame(): List<Game>
}

class GameRepository: IGameRepository {
    private val games = mutableMapOf<String, Game>()

    override fun createGame(gameId: String): Game {
        val newGame = Game(gameId)
        games[gameId] = newGame
        return newGame
    }

    override fun saveGame(game: Game) {
        games[game.id] = game
    }

    override fun deleteGame(gameId: String): Game? {
        return games.remove(gameId)
    }

    override fun findGameById(gameId: String): Game? {
        return games[gameId]
    }

    override fun findAllGame(): List<Game> {
        return games.values.toList()
    }
}