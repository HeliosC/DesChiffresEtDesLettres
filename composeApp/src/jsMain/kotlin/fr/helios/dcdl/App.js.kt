package fr.helios.dcdl

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import fr.helios.dcdl.discord.setupDiscordSdk
import fr.helios.dcdl.game.GameScreen
import fr.helios.dcdl.js.KotlinJs
import fr.helios.dcdl.model.PlayerUI
import fr.helios.dcdl.network.ApiClient
import fr.helios.dcdl.network.GameApi
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.w3c.dom.events.EventListener
import org.w3c.dom.get


@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    setupDiscordSdk()

    val host: String = window["__discordHost"]
    ApiClient.setInstance(host)

    val isReady = js("!!window.__discordReady") as Boolean
    if (isReady) {
        launchApp()
    } else {
        window.addEventListener("discordReady", EventListener {
            launchApp()
        })
    }
}

@OptIn(ExperimentalComposeUiApi::class, DelicateCoroutinesApi::class)
fun launchApp() {
    val activityChannelId: String = window["__activityChannelId"]
    val user: PlayerUI = KotlinJs.decodeJsObject(window["__user"])

    GlobalScope.async {
        val game = GameApi.joinOrCreate(gameId = activityChannelId, userId = user.id, username = user.username)

        ComposeViewport("composeApp") {
            GameScreen(
                activityChannelId,
                user
            )
        }
    }
}
