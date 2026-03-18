package dev.octogene.pooly

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import dev.octogene.pooly.login.LoginScreen
import dev.octogene.pooly.settings.SettingsScreen
import dev.octogene.pooly.ui.lightColorScheme
import dev.zacsweers.metrox.viewmodel.metroViewModel

class Destination(val name: String, val ui: @Composable () -> Unit)

internal class Heading(val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(viewModel: MainViewModel = metroViewModel()) {
    val backStack = remember { mutableStateListOf<Any>("root") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Current winnings")
                },
                navigationIcon = {
                    if (backStack.size > 1) {
                        IconButton(onClick = { backStack.removeAt(backStack.size - 1) }) {
                            Icon(
                                painterResource(R.drawable.baseline_arrow_back_24),
                                contentDescription = "Localized description",
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            backStack.add(Destination("settings") { SettingsScreen() })
                        },
                    ) {
                        Icon(
                            painterResource(R.drawable.baseline_add_24),
                            contentDescription = "Localized description",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        AppNavContainer(innerPadding, backStack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: MainViewModel = metroViewModel()) {
    val isLoggedIn by viewModel.state.collectAsState()

    MaterialTheme(colorScheme = lightColorScheme) {
        when (isLoggedIn) {
            MainUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            MainUiState.LoggedIn -> {
                MainScaffold(viewModel)
            }

            MainUiState.LoggedOut -> {
                LoginScreen()
            }
        }
    }
}

@Composable
private fun AppNavContainer(innerPadding: PaddingValues, backStack: SnapshotStateList<Any>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding()),
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeAt(backStack.size - 1)
                }
            },
            entryProvider = { key ->
                when (key) {
                    "root" -> NavEntry(key) {
                        PrizesScreen {
                            backStack.add(it)
                        }
                    }

                    is Destination -> NavEntry(key, contentKey = key.name) {
                        key.ui()
                    }

                    else -> {
                        error("Unknown route: $key")
                    }
                }
            },
        )
    }
}
