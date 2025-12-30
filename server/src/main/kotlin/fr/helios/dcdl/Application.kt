package fr.helios.dcdl

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(CORS) {
        allowHost("0.0.0.0:8081")
        allowHost("localhost:8081")
        //anyHost()

        //allowHeaders { true }
        //allowHeader(HttpHeaders.ContentType)
        //allowHeader(HttpHeaders.Authorization)
    }

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        get("api/hello") {
            call.respondText("yo")
        }
    }
}