package fr.helios.dcdl

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import fr.helios.dcdl.network.ApiClient
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    val host = if (ApiClient.PROD_ENV) ApiClient.SEVER_URL_DOMAIN_PROD else ApiClient.SEVER_URL_DOMAIN_TEST
    ApiClient.setInstance(host)

    val body = document.body ?: return
    ComposeViewport(body) {
        App(
            onNavHostReady = { it.bindToBrowserNavigation() }
        )
    }
}
