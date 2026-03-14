package fr.helios.dcdl.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingCall

suspend fun <T> RoutingCall.handleResponse(
    result: Result<T>,
    successCallback: suspend ((T) -> Unit) = {},
    createResponse: (T) -> Any
) {
    if (result.isSuccess) {
        val data = result.getOrThrow()
        this.respond(createResponse.invoke(data))
        successCallback.invoke(data)
    } else {
        this.respond(HttpStatusCode.BadRequest, result.exceptionOrNull()?.message ?: "Error")
    }
}
