package dev.octogene.pooly

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import dev.octogene.pooly.model.PrizeUi
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.datetime.LocalDate
import androidx.compose.ui.Modifier.Companion as Modifier

@Composable
fun PrizesScreen(
    prizeViewModel: PrizeViewModel = metroViewModel(),
    modifier: Modifier = Modifier,
    onNavigate: (Destination) -> Unit
) {
    val items = prizeViewModel.prizes.collectAsLazyPagingItems()

    Column(
        modifier = modifier
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (items.itemCount == 0 && items.loadState.refresh is LoadState.NotLoading) {
            EmptyPrizesContent()
        } else {
            LazyColumn {
                if (items.loadState.refresh == LoadState.Loading) {
                    item {
                        Text(
                            text = "Loading prizes",
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally),
                        )
                    }
                }
                items(
                    count = items.itemCount,
                    key = { index ->
                        when (val item = items.peek(index)) {
                            is PrizeListItem.Header -> item.date.toString()
                            is PrizeListItem.Prize -> item.prize.id
                            null -> "loading-$index"
                        }
                    },
                ) { index ->
                    when (val item = items[index]) {
                        is PrizeListItem.Header -> {
                            PrizeHeader(item.date)
                        }

                        is PrizeListItem.Prize -> {
                            PrizeItem(item.prize)
                        }

                        null -> {}
                    }
                }

                if (items.loadState.append == LoadState.Loading) {
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

@Composable
private fun PrizeHeader(date: LocalDate) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Text(
            text = date.toString(),
            textAlign = TextAlign.Companion.Center,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
        )
    }
}

@Composable
private fun PrizeItem(prize: PrizeUi) {
    Text(
        "${prize.formattedPayout} ${prize.vaultSymbol}",
        modifier = Modifier.padding(
            vertical = 4.dp,
            horizontal = 16.dp,
        ),
    )
}

@Composable
private fun EmptyPrizesContent() {
    Column {
        Image(painterResource(R.drawable.pooly), null)
        Text("Nothing to see here")
    }
}
