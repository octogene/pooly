package dev.octogene.pooly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dev.octogene.pooly.model.PrizeUi
import dev.octogene.pooly.model.toUiModel
import dev.octogene.pooly.pooltogether.PrizesRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

@ContributesIntoMap(AppScope::class)
@ViewModelKey(PrizeViewModel::class)
@Inject
class PrizeViewModel(prizesRepository: PrizesRepository) : ViewModel() {
    val prizes: Flow<PagingData<PrizeListItem>> = prizesRepository.getAllDrawsPaged()
        .map { pagingData ->
            pagingData
                .map { prize -> PrizeListItem.Prize(prize.toUiModel()) }
                .insertSeparators { before: PrizeListItem.Prize?, after: PrizeListItem.Prize? ->
                    when {
                        before == null && after != null -> {
                            PrizeListItem.Header(after.prize.date)
                        }

                        before != null && after != null && before.prize.date != after.prize.date -> {
                            PrizeListItem.Header(after.prize.date)
                        }

                        // No separator needed
                        else -> null
                    }
                }
        }
        .cachedIn(viewModelScope)
}

sealed class PrizeListItem {
    data class Header(val date: LocalDate) : PrizeListItem()
    data class Prize(val prize: PrizeUi) : PrizeListItem()
}
