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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.octogene.pooly.model.PrizeUi
import dev.octogene.pooly.settings.SettingsScreen
import dev.octogene.pooly.ui.lightColorScheme
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.datetime.LocalDate

private class Destination(val name: String, val ui: @Composable () -> Unit)

private class Heading(val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: MainViewModel = metroViewModel()) {
    MaterialTheme(colorScheme = lightColorScheme) {
        val allDraws = viewModel.prizes.collectAsLazyPagingItems()
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
    draws: LazyPagingItems<PrizeUi>,
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
private fun RootUi(draws: LazyPagingItems<PrizeUi>, modifier: Modifier = Modifier, onNavigate: (Destination) -> Unit) {
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
            val drawsByDate = remember(draws.itemCount) {
                val map = mutableMapOf<LocalDate, MutableList<PrizeUi>>()
                for (i in 0 until draws.itemCount) {
                    val prize = draws[i]
                    if (prize != null) {
                        map[prize.date] = map.getOrDefault(prize.date, mutableListOf()).apply {
                            add(prize)
                        }
                    }
                }
                map.toMap()
            }
            LazyColumn {
                if (draws.loadState.refresh == LoadState.Loading) {
                    item {
                        Text(
                            text = "Waiting for items to load from the backend",
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally),
                        )
                    }
                }
                drawsByDate.forEach { (date, dailyDraws) ->
                    stickyHeader {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            Text(
                                text = date.toString(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 16.dp),
                            )
                        }
                    }
                    items(dailyDraws.size) { index ->
                        val prize = dailyDraws[index]
                        Text(
                            "${prize.formattedPayout} ${prize.vaultSymbol}",
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                        )
                    }
                }

                if (draws.loadState.append == LoadState.Loading) {
                    item {
                        CircularProgressIndicator(
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally),
                        )
                    }
                }
            }
        }
    }
}
