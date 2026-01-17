package dev.octogene.pooly.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.octogene.pooly.core.ChainNetwork
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = metroViewModel(),
) {
    val activeNetworks by viewModel.activeNetworks.collectAsStateWithLifecycle(emptyList<ChainNetwork>())
    val trackedWallets by viewModel.trackedWallets.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Icon(
                    painterResource(dev.octogene.pooly.common.mobile.R.drawable.baseline_add_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Networks")
            Chains(activeNetworks, { viewModel.toggleNetwork(it) })
            Text("Wallets")
            Wallets(trackedWallets)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Chains(
    networks: List<ChainNetwork>,
    onNetwork: (ChainNetwork) -> Unit,
    modifier: Modifier = Modifier,
) {
    val allNetworks = remember { ChainNetwork.entries.toTypedArray() }

    FlowRow(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (i in 0 until allNetworks.size) {
            val icon = when (allNetworks[i]) {
                ChainNetwork.BASE -> dev.octogene.pooly.common.mobile.R.drawable.bc_base
                ChainNetwork.OPTIMISM -> dev.octogene.pooly.common.mobile.R.drawable.bc_optimism
                ChainNetwork.ARBITRUM -> dev.octogene.pooly.common.mobile.R.drawable.bc_arbitrum_one
                ChainNetwork.SCROLL -> dev.octogene.pooly.common.mobile.R.drawable.bc_scroll
                ChainNetwork.GNOSIS -> dev.octogene.pooly.common.mobile.R.drawable.bc_gnosis
                ChainNetwork.WORLD -> dev.octogene.pooly.common.mobile.R.drawable.bc_world
            }
            NetworkButton(
                icon,
                allNetworks[i] in networks,
                allNetworks[i],
                onNetwork
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NetworkButton(
    icon: Int,
    active: Boolean,
    network: ChainNetwork,
    onClick: (ChainNetwork) -> Unit,
    modifier: Modifier = Modifier,
) {
    ToggleButton(
        checked = active,
        modifier = modifier.size(64.dp),
        shapes = ToggleButtonShapes(
            shape = CircleShape,
            pressedShape = CircleShape,
            checkedShape = RectangleShape
        ),
        onCheckedChange = { onClick(network) },
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = network.name,
                modifier = Modifier.size(24.dp)
            )
            Text(
                network.name.capitalize(Locale.current),
                style = TextStyle(
                    color = Color.Red,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.W800,
                    fontStyle = FontStyle.Italic,
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun Wallets(wallets: List<String>, modifier: Modifier = Modifier) {
    LazyColumn(modifier.padding(8.dp)) {
        items(wallets.size) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    text = wallets[it],
                    overflow = TextOverflow.MiddleEllipsis,
                    maxLines = 1
                )
                IconButton(
//                    modifier = Modifier.weight(0.5f),
                    onClick = { /*TODO*/ }
                ) {
                    Icon(
                        painterResource(dev.octogene.pooly.common.mobile.R.drawable.baseline_delete_24),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ChainsPreview() {
    MaterialTheme {
        Chains(
            networks = listOf(ChainNetwork.BASE, ChainNetwork.OPTIMISM),
            onNetwork = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
