package dev.octogene.pooly

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import co.touchlab.kermit.Logger
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.settings.SettingsScreen
import dev.octogene.pooly.shared.model.Draw
import dev.octogene.pooly.ui.lightColorScheme
import dev.zacsweers.metrox.viewmodel.metroViewModel

private class Destination(
    val name: String,
    val ui: @Composable () -> Unit
)

private class Heading(val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: MainViewModel = metroViewModel()) {
    MaterialTheme(colorScheme = lightColorScheme) {
        val draws by viewModel.draws.collectAsState()
        val backStack = remember { mutableStateListOf<Any>("root") }
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Navigation example",
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { backStack.removeLastOrNull() }) {
                            Icon(
                                painterResource(R.drawable.baseline_arrow_back_24),
                                contentDescription = "Localized description"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                backStack.add(Destination("settings") { SettingsScreen() })
                            }
                        ) {
                            Icon(
                                painterResource(R.drawable.baseline_add_24),
                                contentDescription = "Localized description"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            AppNavContainer(innerPadding, backStack, draws)
        }
    }
}

@Composable
private fun AppNavContainer(
    innerPadding: PaddingValues,
    backStack: SnapshotStateList<Any>,
    draws: Map<ChainNetwork, List<Draw>>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding())
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = { key ->
                when (key) {
                    "root" -> NavEntry(key) {
                        RootUi(draws) {
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
            }
        )
    }
}

@Composable
private fun RootUi(
    draws: Map<ChainNetwork, List<Draw>>,
    modifier: Modifier = Modifier,
    onNavigate: (Destination) -> Unit
) {
    LaunchedEffect(Unit) {
        Logger.i { draws.toString() }
    }

    Column(
        modifier = modifier
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column {
            Image(painterResource(R.drawable.pooly), null)
            Text("Nothing to see here")
        }
    }
}

// @Composable
// private fun WinsUi(
//    wins: List<Draw>,
//    modifier: Modifier = Modifier
// ) {
//    val state by remember {
//        derivedStateOf { wins.isEmpty() }
//    }
//    AnimatedContent(state) { isWins ->
//        if (isWins) {
//            LazyColumn {
//                items(wins) { win ->
//                    Text(win.winner)
//                }
//            }
//        } else {
//            Text("No wins yet")
//        }
//    }
// }
