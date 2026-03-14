package fr.helios.dcdl.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class DiscordOauthRequest(
    val code: String,
    @SerialName("grant_type") val grantType: String = "authorization_code"
)

@Serializable
data class DiscordOauthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("refresh_token") val refreshToken: String,
    val scope: String
)
