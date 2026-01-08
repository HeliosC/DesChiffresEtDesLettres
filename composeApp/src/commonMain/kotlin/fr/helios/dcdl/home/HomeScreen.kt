package fr.helios.dcdl.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import fr.helios.dcdl.home.create.CreateGameUI
import fr.helios.dcdl.home.create.CreateGameViewModel
import fr.helios.dcdl.home.join.JoinGameUI
import fr.helios.dcdl.home.join.JoinGameViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun HomeScreen(
    navigateToDashboard: (String) -> Unit,
    navigateToGame: (String, String) -> Unit,
    modifier: Modifier
) {
    val createGameViewModel: CreateGameViewModel = viewModel { CreateGameViewModel() }
    val createGameIdState = rememberTextFieldState()

    val joinGameViewModel: JoinGameViewModel = viewModel { JoinGameViewModel() }
    val joinGameIdState = rememberTextFieldState()
    val joinUsernameState = rememberTextFieldState()

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> {
            Row(
                modifier = modifier
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .safeContentPadding()
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CreateGameUI(createGameIdState, navigateToDashboard, createGameViewModel)
                Spacer(Modifier.width(16.dp))
                JoinGameUI(joinGameIdState, joinUsernameState, navigateToGame, joinGameViewModel)
            }
        }

        else -> {
            Column(
                modifier = modifier
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .safeContentPadding()
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CreateGameUI(createGameIdState, navigateToDashboard, createGameViewModel)
                Spacer(Modifier.height(16.dp))
                JoinGameUI(joinGameIdState, joinUsernameState, navigateToGame, joinGameViewModel)
            }
        }
    }
}