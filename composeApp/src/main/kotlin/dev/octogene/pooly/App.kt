package dev.octogene.pooly

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import dev.octogene.pooly.common.mobile.ui.Destination
import dev.octogene.pooly.login.loginScreenDestination
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun MainScaffold(backStack: SnapshotStateList<Destination>) {
    val currentDestination by remember {
        derivedStateOf { backStack.lastOrNull() }
    }

    Scaffold(
        topBar = { currentDestination?.topBar?.invoke() },
    ) { innerPadding ->
        AppNavContainer(innerPadding, backStack)
    }
}

@Composable
fun App(viewModel: MainViewModel = metroViewModel()) {
    val backStack = remember {
        mutableStateListOf(loadingScreenDestination())
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        when (state) {
            MainUiState.LoggedIn -> {
                backStack.clear()
                backStack.add(prizeScreenDestination(backStack))
            }

            MainUiState.LoggedOut -> {
                backStack.clear()
                backStack.add(loginScreenDestination(backStack))
            }

            MainUiState.Loading -> {
                backStack.clear()
                backStack.add(loadingScreenDestination())
            }
        }
    }

    MainScaffold(backStack)
}

@Composable
private fun AppNavContainer(
    innerPadding: PaddingValues,
    backStack: SnapshotStateList<Destination>
) {
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
            transitionSpec = {
                slideInHorizontally(initialOffsetX = { it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it })
            },
            popTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            },
            predictivePopTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            },
            entryProvider = { key ->
                NavEntry(key, contentKey = key.name) {
                    key.ui()
                }
            },
        )
    }
}
