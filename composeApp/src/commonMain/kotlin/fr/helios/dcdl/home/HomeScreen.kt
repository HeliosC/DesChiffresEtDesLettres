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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import fr.helios.dcdl.home.create.CreateGameUI
import fr.helios.dcdl.home.join.JoinGameUI
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun HomeScreen(
    navigateToDashboard: (String) -> Unit,
    navigateToGame: (String, String) -> Unit,
    modifier: Modifier
) {
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
                CreateGameUI(navigateToDashboard)
                Spacer(Modifier.width(16.dp))
                JoinGameUI(navigateToGame)
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
                CreateGameUI(navigateToDashboard)
                Spacer(Modifier.height(16.dp))
                JoinGameUI(navigateToGame)
            }
        }
    }
}