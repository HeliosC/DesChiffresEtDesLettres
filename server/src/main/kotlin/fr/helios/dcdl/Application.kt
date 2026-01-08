package fr.helios.dcdl

import fr.helios.dcdl.repository.GameRepository
import fr.helios.dcdl.route.gameRoute
import fr.helios.dcdl.service.GameService
import fr.helios.dcdl.websocket.GameUpdateBroadcaster
import fr.helios.dcdl.websocket.GameWebSocketHandler
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(
        factory = Netty,
        port = SERVER_PORT,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureApi()
    configureWebSockets()

    //TODO: better option for circular dependencies??
    val gameRepository = GameRepository()
    val wsHandler = GameWebSocketHandler(
        GameService(
            gameRepository,
            object : GameUpdateBroadcaster {
                override suspend fun broadcastGameUpdate(gameId: String) { }
            }
        )
    )
    val gameService = GameService(gameRepository, wsHandler)
    wsHandler.setGameService(gameService)

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        gameRoute(gameService)
        wsHandler.webSocketRoutes(this)
    }
}

fun Application.configureApi() {
    install(ContentNegotiation) {
        json(getJson())
    }
    install(CORS) {
        allowHost("0.0.0.0:8081")
        allowHost("localhost:8081")
        anyHost()

        //allowHeaders { true }
        allowHeader(HttpHeaders.ContentType)
        //allowHeader(HttpHeaders.Authorization)
    }
}

fun Application.configureWebSockets() {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(getJson())
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}

private fun getJson(): Json {
    return Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }
}
