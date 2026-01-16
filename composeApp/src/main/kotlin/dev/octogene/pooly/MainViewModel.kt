package dev.octogene.pooly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.pooltogether.PoolTogetherRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

@ContributesIntoMap(AppScope::class)
@ViewModelKey(MainViewModel::class)
@Inject
class MainViewModel(
    private val poolTogetherRepository: PoolTogetherRepository
) : ViewModel() {
    val draws: StateFlow<List<Prize>> = poolTogetherRepository.getAllDraws()
        .onEach { Logger.i { "New flow with ${it.size} prizes" } }
        .map { it.take(10) }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )
}
