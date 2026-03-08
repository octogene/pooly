package dev.octogene.pooly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dev.octogene.pooly.model.toUiModel
import dev.octogene.pooly.pooltogether.PrizesRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.map

@ContributesIntoMap(AppScope::class)
@ViewModelKey(MainViewModel::class)
@Inject
class MainViewModel(prizesRepository: PrizesRepository) : ViewModel() {
    val prizes = prizesRepository.getAllDrawsPaged().map { pagingData ->
        pagingData.map { prize -> prize.toUiModel() }
    }.cachedIn(viewModelScope)
}
