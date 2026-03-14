package fr.helios.dcdl.network

import fr.helios.dcdl.dto.DiscordOauthRequest
import io.ktor.client.request.basicAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.formUrlEncode

object DiscordApi {

    suspend fun getToken(clientId: String, clientSecret: String, code: String) =
        DiscordApiClient.client.post("oauth2/token") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)

            val data = DiscordOauthRequest(code = code)
            setBody(
                Parameters.build {
                    append("grant_type",    data.grantType)
                    append("code",          data.code)
                }.formUrlEncode()
            )

            basicAuth(clientId, clientSecret)
        }
}
