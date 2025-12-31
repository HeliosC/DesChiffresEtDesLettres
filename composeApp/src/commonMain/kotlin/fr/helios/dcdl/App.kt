package fr.helios.dcdl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

import fr.helios.dcdl.network.ApiClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    MaterialTheme {
        val coroutineScope = rememberCoroutineScope()

        var responseTestApi by remember { mutableStateOf("waiting...") }

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                try {
                    val httpResponse: HttpResponse = ApiClient.client.get("api/hello")
                    println("Success: ${httpResponse.bodyAsText()}")
                    responseTestApi = httpResponse.bodyAsText()
                } catch (e: Exception) {
                    responseTestApi = "Erreur: ${e.message}"
                }
            }
        }

        val textFiledState = rememberTextFieldState()

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("game ID:")

            BasicTextField(
                state = textFiledState
            )

            Button(onClick = {

            }) {
                Text("Join game !")
            }
        }
    }
}