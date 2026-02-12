package dev.octogene.pooly

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.settings.SettingsScreen
import dev.octogene.pooly.ui.lightColorScheme
import dev.zacsweers.metrox.viewmodel.metroViewModel
import java.math.BigDecimal
import java.math.RoundingMode

private class Destination(val name: String, val ui: @Composable () -> Unit)

private class Heading(val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: MainViewModel = metroViewModel()) {
    MaterialTheme(colorScheme = lightColorScheme) {
        val draws by viewModel.draws.collectAsState()
        val allDraws = viewModel.alldraws.collectAsLazyPagingItems()
        val backStack = remember { mutableStateListOf<Any>("root") }
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Current winnings")
                    },
                    navigationIcon = {
                        IconButton(onClick = { backStack.removeLastOrNull() }) {
                            Icon(
                                painterResource(R.drawable.baseline_arrow_back_24),
                                contentDescription = "Localized description",
                            )
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
            AppNavContainer(innerPadding, backStack, allDraws)
        }
    }
}

@Composable
private fun AppNavContainer(
    innerPadding: PaddingValues,
    backStack: SnapshotStateList<Any>,
    draws: LazyPagingItems<Prize>,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding()),
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
            },
        )
    }
}

@Composable
private fun RootUi(draws: LazyPagingItems<Prize>, modifier: Modifier = Modifier, onNavigate: (Destination) -> Unit) {
    Column(
        modifier = modifier
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (draws.itemCount == 0) {
            Column {
                Image(painterResource(R.drawable.pooly), null)
                Text("Nothing to see here")
            }
        } else {
            LazyColumn {
                if (draws.loadState.refresh == LoadState.Loading) {
                    item {
                        Text(
                            text = "Waiting for items to load from the backend",
                            modifier =
                            Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally),
                        )
                    }
                }
                items(draws.itemCount) { index ->
                    draws[index]?.payout?.let { amount ->
                        Text(
                            "${draws[index]?.timestamp} ${
                                BigDecimal(amount)
                                    .divide(BigDecimal.TEN.pow(16))
                                    .setScale(5, RoundingMode.HALF_DOWN)
                                    .toPlainString()
                            } ${draws[index]?.vault?.symbol}",
                        )
                    }
                }

                if (draws.loadState.append == LoadState.Loading) {
                    item {
                        CircularProgressIndicator(
                            modifier =
                            Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally),
                        )
                    }
                }
            }
        }
    }
}
