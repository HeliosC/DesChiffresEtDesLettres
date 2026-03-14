package fr.helios.dcdl.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordTokenRequest(val code: String)

@Serializable
data class DiscordTokenResponse(@SerialName("access_token") val accessToken: String)
