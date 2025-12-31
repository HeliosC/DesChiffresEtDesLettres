package fr.helios.dcdl

import fr.helios.dcdl.repository.GameRepository
import fr.helios.dcdl.route.gameRoute
import fr.helios.dcdl.service.GameService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.net.http.WebSocket

fun main() {
    embeddedServer(
        factory = Netty,
        port = SERVER_PORT,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        })
    }
    install(CORS) {
        allowHost("0.0.0.0:8081")
        allowHost("localhost:8081")
        //anyHost()

        //allowHeaders { true }
        allowHeader(HttpHeaders.ContentType)
        //allowHeader(HttpHeaders.Authorization)
    }
    //install(WebSockets)

    val gameService = GameService(GameRepository())
    //WS

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        gameRoute(gameService)
        //ws
    }
}